#include  <SPI.h>
#include  <RF24.h>
#include  "packet.h"

#define BUFFER_SIZE 70

RF24 radio(9,10);

unsigned int pos = 0;
payload_t ppl[BUFFER_SIZE]; // Données non expédiées

uint32_t wait = 2000;
uint32_t lastMeasureTime = 0;

uint8_t sensorLumP = A1; //Pour le capteur de luminosité P
uint8_t sensorPHPin = A2;
uint8_t sensorFlowPin = 3;  //The pin location of the sensor
uint8_t sensorLumS = A0; //Pour le capteur de luminosité S

uint8_t pHCalibrationPin = 2;
uint8_t pHCalibrationLED = 4;
uint8_t redLED = 5;
uint8_t greenLED = 6;

boolean buttonState;             // the current reading from the input pin
boolean lastButtonState = LOW;   // the previous reading from the input pin
unsigned long lastDebounceTime = 0;  // the last time the output pin was toggled
unsigned long debounceDelay = 50;    // the debounce time; increase if the output flickers

enum class Mode : uint8_t {
  Measure,
  PHCalibration,
} mode;

enum class CalibrationState : uint8_t {
  PH4,
  PH7
} calibrationState;

//Pour le débimètre 
volatile unsigned int NbTopsFan = 0; //measuring the rising edges of the signal

int measureFlow() //Pour mesure le débit avec le débitmètre
{
  NbTopsFan = 0;   //Set NbTops to 0 ready for calculations
  attachInterrupt(digitalPinToInterrupt(sensorFlowPin), rpm, RISING); //interrupt is attached
  delay (1000);   //Wait 1 second
  detachInterrupt(digitalPinToInterrupt(sensorFlowPin)); //interrupt is detached
  return NbTopsFan;
}

void rpm()     //This is the function that the interupt calls
{
  NbTopsFan++;  //This function measures the rising and falling edge of the hall effect sensors signal
}

uint16_t analogReadAvg(uint8_t pin, unsigned int num, unsigned int d) {
  analogRead(pin); //discard first reading
  uint32_t sum = 0;
  for (unsigned int i = 0; i < num; i++) {
    sum += analogRead(pin);
    delay(d);
  }

   return sum/num;
}

uint16_t analogReadAvg(uint8_t pin) {
  return analogReadAvg(pin, 10, 1);
}

uint16_t analogReadPrecise(uint8_t pin) {
  return analogReadAvg(pin, 100, 10);
}

void updateLastMeasure() {
    if (millis() >= lastMeasureTime) {
    for (unsigned int i = 0; i < pos; i++) {
      ppl[i].date += millis() - lastMeasureTime;
    }
  } else { //millis() a rebouclé à 0
    Serial.println(F("Fin d'un cycle de millis()"));
    for (unsigned int i = 0; i < pos; i++) {
      ppl[i].date += (((unsigned int)(-1)) - lastMeasureTime) + millis() ;
    }
  }

  lastMeasureTime = millis();
}

void acquireMeasures() {
  updateLastMeasure();

  Serial.println();
  Serial.println(F("*******************************************************"));
  if (pos < BUFFER_SIZE) {
    digitalWrite(greenLED, HIGH);
    Serial.print(F("Acquisition d'une nouvelle mesure (pos : "));
    Serial.print(pos);
    Serial.println(F(")."));
    //A modifie avec le code de l'interfacage capteur
    ppl[pos].id = PacketID::Measure;
    ppl[pos].date=0;
    ppl[pos].measure.temp=random(0,255);

    //Pour la lumière
    ppl[pos].measure.lumP=analogReadAvg(sensorLumP);
    ppl[pos].measure.lumS=analogReadAvg(sensorLumS);
    
    ppl[pos].measure.flow=measureFlow();  //On récupère la valeur du débit en Litre/heure
    
    ppl[pos].measure.pH=analogReadAvg(sensorPHPin);
    ppl[pos].measure.level=random(0,1023);
    pos++;
    digitalWrite(greenLED, LOW);
  } else {
    Serial.print(F("Buffer plein (pos : "));
    Serial.print(pos);
    Serial.println(F(") !"));
  }
}

void sendMeasures() {
  radio.powerUp();
  
  Serial.println(F("Tentative d'envoi : "));
  unsigned long timer = micros();
  
  unsigned int i = 0;
  while (i < pos) {
    Serial.print(i);
    Serial.print(F("..."));
    if (radio.write(&ppl[i],sizeof(payload_t))) 
    {
      Serial.println(F(" succes !"));
      i++;
    }
    else
    {
      Serial.print(F(" échec ! ")); 
      Serial.print((micros() - timer)*0.001);
      Serial.println("ms");
      for (unsigned int j = i; j < pos; j++) {
        ppl[j-i] = ppl[j];
      }
      break;
    }
  }

  pos -= i;
}

boolean hasBeenPressed() {
  if ((millis() - lastDebounceTime) > debounceDelay) {
      // whatever the reading is at, it's been there for longer
      // than the debounce delay, so take it as the actual current state:
  
      // if the button state has changed:
      if (lastButtonState != buttonState) {
        buttonState = lastButtonState;
  
        return buttonState == HIGH;
      }
  }
  return false;
}

void startPHCalibration() {
  mode = Mode::PHCalibration;
  calibrationState = CalibrationState::PH4;
  digitalWrite(pHCalibrationLED, HIGH);
  digitalWrite(redLED, HIGH);
  Serial.println();
  Serial.println(F("*******************************************************"));
  Serial.println("Début calibration pH...");
  Serial.print(" - Solution PH 4");
}

void pHCalibrate() {
  if (calibrationState == CalibrationState::PH4) {   
    updateLastMeasure();
    ppl[pos].id = PacketID::PHCalibration;
    ppl[pos].date=0;
    ppl[pos].ph_calibration.ph4 = analogReadPrecise(sensorPHPin);

    Serial.print(F(" : "));
    Serial.println(ppl[pos].ph_calibration.ph4);

    calibrationState = CalibrationState::PH7;
    digitalWrite(redLED, LOW);
    digitalWrite(greenLED, HIGH);
    Serial.print(" - Solution PH 7");
  } else {
    ppl[pos].ph_calibration.ph7 = analogReadPrecise(sensorPHPin);

    Serial.print(F(" : "));
    Serial.println(ppl[pos].ph_calibration.ph7);
    
    pos++;
    
    mode = Mode::Measure;
    digitalWrite(greenLED, LOW);
    digitalWrite(pHCalibrationLED, LOW);
    Serial.println("Fin de la calibration pH !");
  }
}

void setup() {
  //Pour le capteur de débit
  pinMode(sensorFlowPin, INPUT); //initializes digital pin as an input

  pinMode(pHCalibrationPin, INPUT_PULLUP);
  pinMode(pHCalibrationLED, OUTPUT);
  pinMode(redLED, OUTPUT);
  pinMode(greenLED, OUTPUT);

  buttonState = !digitalRead(pHCalibrationPin); //Pull UP

  mode = Mode::Measure;
  
  //Pour la transmission série
  Serial.begin(115200);    // Initialiser la communication série 
  Serial.println(F("Debut de la surveillance..."));
  
  //Radio
  radio.begin();
  radio.setChannel(CHANNEL);
  radio.setDataRate(RF24_2MBPS);
  radio.enableDynamicPayloads();
  radio.setPALevel(RF24_PA_MAX);
  radio.setRetries(15,15);            //On met un délai de 4ms et un nombre max de tentatives de 15
  radio.openWritingPipe(ADDRESS);    // Ouvrir le Pipe en écriture
  radio.stopListening();
}

void loop(void) {
  int actualButtonState = !digitalRead(pHCalibrationPin); //Pull UP

  // If the switch changed, due to noise or pressing:
  if (actualButtonState != lastButtonState) {
    // reset the debouncing timer
    lastDebounceTime = millis();
  }

  lastButtonState = actualButtonState;
  
  if (mode == Mode::Measure) {
    digitalWrite(redLED, pos != 0);
    
    if (hasBeenPressed() && pos < BUFFER_SIZE) {
      startPHCalibration();
    }
    else if (millis() - lastMeasureTime > wait) {
      acquireMeasures();
      sendMeasures();
      Serial.print(F("Prochaine mesure dans "));
      Serial.print((wait - (millis() - lastMeasureTime))/1000.);
      Serial.println(F(" secondes..."));
      radio.powerDown();
    }
  } else if (mode == Mode::PHCalibration) {
     if (hasBeenPressed()) {
      pHCalibrate();
    }
  }
}



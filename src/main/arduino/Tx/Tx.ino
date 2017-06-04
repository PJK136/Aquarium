#include  <SPI.h>
#include  <RF24.h>
#include  <OneWire.h>
#include  "packet.h"

#define BUFFER_SIZE 75

RF24 radio(9,10);

uint8_t pos = 0; //MAX_BUFFER_SIZE 255
payload_t ppl[BUFFER_SIZE]; // Données non expédiées

const uint32_t wait = 60000;
uint32_t lastMeasureTime = 0;
uint32_t lastUpdateTime = 0;
const uint32_t sendInterval = 10000;
uint32_t lastSendTime = 0;

const uint8_t sensorLumP = A1; //Pour le capteur de luminosité P
const uint8_t sensorPHPin = A2;
const uint8_t sensorFlowPin = 3;  //The pin location of the sensor
const uint8_t sensorLevelPin = A4;
const uint8_t sensorTempPin = 7;
const uint8_t sensorLumS = A0; //Pour le capteur de luminosité S

const uint8_t pHCalibrationPin = 2;
const uint8_t pHCalibrationLED = 4;
const uint8_t redLED = 5;
const uint8_t greenLED = 6;

boolean buttonState;             // the current reading from the input pin
boolean lastButtonState = LOW;   // the previous reading from the input pin
unsigned long lastDebounceTime = 0;  // the last time the output pin was toggled
const unsigned int debounceDelay = 50;    // the debounce time; increase if the output flickers

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
  delay (5000);   //Wait 5 second
  detachInterrupt(digitalPinToInterrupt(sensorFlowPin)); //interrupt is detached
  return NbTopsFan;
}

void rpm()     //This is the function that the interupt calls
{
  NbTopsFan++;  //This function measures the rising and falling edge of the hall effect sensors signal
}

//Pour capteur de température
OneWire ds(sensorTempPin);

int getRawTemp(){
  //returns the temperature from one DS18S20 in DEG Celsius

  byte data[12];
  byte addr[8];

  if (!ds.search(addr)) {
      //no more sensors on chain, reset search
      ds.reset_search();
      Serial.println(F("No more sensor !"));
      return -1000;
  }

  if (OneWire::crc8(addr, 7) != addr[7]) {
      Serial.println(F("CRC is not valid!"));
      return -1000;
  }

  if (addr[0] != 0x10 && addr[0] != 0x28) {
      Serial.println(F("Device is not recognized"));
      return -1000;
  }

  ds.reset();
  ds.select(addr);
  ds.write(0x44,1); // start conversion, with parasite power on at the end

  byte present = ds.reset();
  ds.select(addr);    
  ds.write(0xBE); // Read Scratchpad

  
  for (int i = 0; i < 9; i++) { // we need 9 bytes
    data[i] = ds.read();
  }
  
  ds.reset_search();
  
  byte MSB = data[1];
  byte LSB = data[0];

  return ((MSB << 8) | LSB);
}

unsigned int getRawTempAvg() {
  uint16_t num = 0;
  uint32_t sum = 0;
  for (unsigned int i = 0; i < 100; i++) {
    int value = getRawTemp();
    if (value >= 0) {
      sum += value;
      num++;
    }
    
    delay(10);
  }

  if (num > 0)
    return sum/num;
  else
    return -1000;
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
  return analogReadAvg(pin, 100, 10);
}

uint16_t analogReadPrecise(uint8_t pin) {
  return analogReadAvg(pin, 100, 10);
}

void updateMeasures() {
  if (millis() >= lastUpdateTime) {
    for (unsigned int i = 0; i < pos; i++) {
      ppl[i].date += millis() - lastUpdateTime;
    }
  } else { //millis() a rebouclé à 0
    Serial.println(F("Fin d'un cycle de millis()"));
    for (unsigned int i = 0; i < pos; i++) {
      ppl[i].date += (((unsigned int)(-1)) - lastUpdateTime) + millis() ;
    }
  }

  lastUpdateTime = millis();
}

void acquireMeasures() {
  updateMeasures();

  lastMeasureTime = millis();
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
    ppl[pos].measure.temp=getRawTempAvg();

    //Pour la lumière
    ppl[pos].measure.lumP=analogReadAvg(sensorLumP);
    ppl[pos].measure.lumS=analogReadAvg(sensorLumS);
    
    ppl[pos].measure.flow=measureFlow();  //On récupère la valeur du débit en Litre/heure
    
    ppl[pos].measure.pH=analogReadAvg(sensorPHPin);

    analogRead(sensorLevelPin); //Discard first measure
    ppl[pos].measure.level=analogReadAvg(sensorPHPin, 10, 10);
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
  
  lastSendTime = millis();

  if (!pos)
    return;

  updateMeasures();

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
      Serial.println(F("ms"));
      for (unsigned int j = i; j < pos; j++) {
        ppl[j-i] = ppl[j];
      }
      break;
    }
  }

  pos -= i;
  radio.powerDown();
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
  Serial.println(F("********************************"));
  Serial.println(F("Début calibration pH..."));
  Serial.print(F(" - Solution PH 4"));
}

void pHCalibrate() {
  if (calibrationState == CalibrationState::PH4) {   
    updateMeasures();
    ppl[pos].id = PacketID::PHCalibration;
    ppl[pos].date=0;
    ppl[pos].ph_calibration.ph4 = analogReadPrecise(sensorPHPin);

    Serial.print(F(" : "));
    Serial.println(ppl[pos].ph_calibration.ph4);

    calibrationState = CalibrationState::PH7;
    digitalWrite(redLED, LOW);
    digitalWrite(greenLED, HIGH);
    Serial.print(F(" - Solution PH 7"));
  } else {
    ppl[pos].ph_calibration.ph7 = analogReadPrecise(sensorPHPin);

    Serial.print(F(" : "));
    Serial.println(ppl[pos].ph_calibration.ph7);
    
    pos++;
    
    mode = Mode::Measure;
    digitalWrite(greenLED, LOW);
    digitalWrite(pHCalibrationLED, LOW);
    Serial.println(F("Fin de la calibration pH !"));
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

  getRawTempAvg(); //Discard first measures
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
    else if (lastMeasureTime == 0 || millis() - lastMeasureTime > wait) {
      acquireMeasures();
      sendMeasures();
      Serial.print(F("Prochaine mesure dans "));
      Serial.print((wait - (millis() - lastMeasureTime))/1000.);
      Serial.println(F(" secondes..."));
    }
    else if (lastSendTime == 0 || millis() - lastSendTime > sendInterval) {
      sendMeasures();
    }
  } else if (mode == Mode::PHCalibration) {
     if (hasBeenPressed()) {
      pHCalibrate();
    }
  }
}



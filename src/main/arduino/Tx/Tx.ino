#include  <SPI.h>
#include  <RF24.h>
#include  "packet.h"

#define BUFFER_SIZE 20

RF24 radio(9,10);

unsigned int pos = 0;
payload_t ppl[BUFFER_SIZE]; // Données non expédiées

uint32_t wait = 1000;
uint32_t lastUpdate = 0;

//Pour le débimètre 
volatile int NbTopsFan = 0; //measuring the rising edges of the signal
int hallsensor = 2;    //The pin location of the sensor

int measureFlow() //Pour mesure le débit avec le débitmètre
{
  NbTopsFan = 0;   //Set NbTops to 0 ready for calculations
  attachInterrupt(digitalPinToInterrupt(hallsensor), rpm, RISING); //interrupt is attached
  delay (1000);   //Wait 1 second
  detachInterrupt(digitalPinToInterrupt(hallsensor)); //interrupt is detached
  int Calc = (NbTopsFan * 60 / 5.5); //(Pulse frequency x 60) / 5.5Q, = flow rate in L/hour
  return Calc;
}

//Pour le capteur de luminosité n°1
int sensorLumPin = A2; // select the input pin for LDR

void rpm()     //This is the function that the interupt calls
{
  NbTopsFan++;  //This function measures the rising and falling edge of the hall effect sensors signal
}

void setup() {
  //Pour le capteur de débit
  pinMode(hallsensor, INPUT); //initializes digital pin 2 as an input
  
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
  Serial.println(F("****************"));

  if (millis() >= lastUpdate) {
    for (unsigned int i = 0; i < pos; i++) {
      ppl[i].date += millis() - lastUpdate;
    }
  } else { //millis() a rebouclé à 0
    Serial.println(F("Fin d'un cycle de millis()"));
    for (unsigned int i = 0; i < pos; i++) {
      ppl[i].date += (((unsigned int)(-1)) - lastUpdate) + millis() ;
    }
  }

  lastUpdate = millis();
  
  if (pos < BUFFER_SIZE) {
    Serial.print(F("Acquisition d'une nouvelle mesure (pos : "));
    Serial.print(pos);
    Serial.println(F(")."));
    //A modifie avec le code de l'interfacage capteur
    ppl[pos].id=MAGIC_ID;
    ppl[pos].date=0;
    ppl[pos].temp=random(0,255);

    //Pour la lumière
    ppl[pos].lum=analogRead(sensorLumPin);
    
    ppl[pos].flow=measureFlow();  //On récupère la valeur du débit en Litre/heure
    
    ppl[pos].pH=random(0,1023);
    ppl[pos].level=random(0,1023);
    pos++;
  } else {
    Serial.print(F("Buffer plein (pos : "));
    Serial.print(pos);
    Serial.println(F(") !"));
  }
  
  Serial.println(F("Tentation d'envoi : "));
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

  Serial.print(F("Attente de "));
  Serial.print(wait/1000);
  Serial.println(F(" secondes..."));
  delay(wait);
}



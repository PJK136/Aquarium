#include  <SPI.h>
#include  <RF24.h>
#include  "packet.h"

#define BUFFER_SIZE 20

RF24 radio(9,10);

unsigned int pos = 0;
payload_t ppl[BUFFER_SIZE]; // Données non expédiées

unsigned int wait = 3000;

//Pour le débimètre 
volatile int NbTopsFan; //measuring the rising edges of the signal
int Calc;
int hallsensor = 2;    //The pin location of the sensor

//Pour le capteur de luminosité n°1
int sensorLumPin = A0; // select the input pin for LDR

void rpm ()     //This is the function that the interupt calls
{
    NbTopsFan++;  //This function measures the rising and falling edge of the hall effect sensors signal
}

void setup() {
  //Pour le capteur de débit
  pinMode(hallsensor, INPUT); //initializes digital pin 2 as an input
  attachInterrupt(0, rpm, RISING); //and the interrupt is attached
  //Pour la transmission en elle-même
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
    
    //Pour le débimètre (la mesure)
    NbTopsFan = 0;   //Set NbTops to 0 ready for calculations
    sei();      //Enables interrupts
    delay (1000);   //Wait 1 second
    cli();      //Disable interrupts
    Calc = (NbTopsFan * 60 / 5.5); //(Pulse frequency x 60) / 5.5Q, = flow rate in L/hour
    ppl[pos].flow=Calc;           //On récupère la valeur du débit en Litre/heure
    
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
  
  int i = 0;
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
      for (int j = i; j < pos; j++) {
        ppl[j-i] = ppl[j];
      }
      break;
    }
  }

  pos -= i;

  for (int i = 0; i < pos; i++) {
    ppl[i].date++;
  }

  Serial.print(F("Attente de "));
  Serial.print(wait/1000);
  Serial.println(F(" secondes..."));
  delay(wait);
}



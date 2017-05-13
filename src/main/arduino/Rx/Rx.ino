#include  <SPI.h>
#include  <RF24.h>
#include  "packet.h"

RF24 radio(9,10); 
payload_t pl;

int ID_LUM =1;
int ID_PH =2;
int ID_FLOW=3;
int ID_LVL=4;
int ID_TEMP=5;

boolean isListening = false;
unsigned long lastSerialAck = 0;
#define TIMEOUT 10000

boolean forceStop = false;

//Affichage des données simplifié

void printPadding(unsigned int value, unsigned int space) {
  //Padding
  int size = max(1, ceil(log10(value+1)));
  for (int j = 0;j<(space-size);j++){
    Serial.print(" ");
  }
}
void printSerialMeasure(unsigned int id, unsigned int value) {
  
  Serial.print(id);
  Serial.print(" : ");
  Serial.print(value);
  Serial.print(",");
  printPadding(value, 10);
}

void setup() {
  Serial.begin(115200);    // Initialiser la communication série 
  Serial.println(F("Debut du programme."));
  
  radio.begin();
  radio.setChannel(CHANNEL);
  radio.setDataRate(RF24_2MBPS);
  radio.enableDynamicPayloads();
  radio.setPALevel(RF24_PA_MAX);
  radio.setRetries(15,15);            //On met un délai de 4ms et un nombre max de tentatives de 15
  
  radio.openReadingPipe(0, ADDRESS); // Ouvrir le Pipe en lecture
  radio.stopListening();
}

void loop(void) {
  if (Serial.available()) {
    char r = Serial.read();  
    if (r == '0') {
      forceStop = true;
      radio.stopListening();
      Serial.println(F("Forced stop listening"));
    } else if (r == '1') {
      forceStop = false;
      Serial.println(F("Back to automatic state"));
      if (isListening)
        radio.startListening();
    } else if (r == 'A') {
      lastSerialAck = millis();
      if (!isListening && !forceStop) {
        Serial.println(F("Start listening : got Serial ACK"));
        radio.startListening();
        isListening = true;
      }
    }
  }

  if (isListening && millis() - lastSerialAck > TIMEOUT) {
      Serial.println(F("Stop listening : serial communication timed out"));
      radio.stopListening();
      isListening = false;
  }
  

  while (isListening && !forceStop && radio.available()) 
  {
    radio.read(&pl, sizeof(payload_t));
    Serial.print(F("Measures : "));
    Serial.print(pl.date);
    Serial.print(",");
    printPadding(pl.date, 10);
    
    printSerialMeasure(ID_LUM,pl.lum);
    printSerialMeasure(ID_PH,pl.pH);
    printSerialMeasure(ID_FLOW,pl.flow);
    printSerialMeasure(ID_LVL,pl.level);
    printSerialMeasure(ID_TEMP,pl.temp);
    Serial.println();
  }
}


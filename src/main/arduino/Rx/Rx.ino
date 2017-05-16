#include  <SPI.h>
#include  <RF24.h>
#include  "packet.h"

RF24 radio(9,10); 
payload_t pl;

unsigned int ID_LUM =1;
unsigned int ID_PH =2;
unsigned int ID_FLOW=3;
unsigned int ID_LVL=4;
unsigned int ID_TEMP=5;

boolean isListening = false;
unsigned long lastSerialAck = 0;
#define TIMEOUT 2500

boolean forceStop = false;

//Affichage des données simplifié

void printPadding(unsigned int value, unsigned int space) {
  //Padding
  unsigned int size = max(1, ceil(log10(value+1)));
  if (size > space)
    return;
    
  for (unsigned int j = 0;j<(space-size);j++){
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
  Serial.begin(38400);    // Initialiser la communication série
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
    } else if (r == 'B') {
      Serial.println(F("Stop listening : got Serial Disconnect"));
      radio.stopListening();
      isListening = false;
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
    if (pl.id == PacketID::Measure)
    {
      Serial.print(F("Measures : "));
      Serial.print(pl.date);
      Serial.print(",");
      printPadding(pl.date, 10);
      
      printSerialMeasure(ID_LUM,pl.measure.lum);
      printSerialMeasure(ID_PH,pl.measure.pH);
      printSerialMeasure(ID_FLOW,pl.measure.flow);
      printSerialMeasure(ID_LVL,pl.measure.level);
      printSerialMeasure(ID_TEMP,pl.measure.temp);
    } else if (pl.id == PacketID::PHCalibration) {
      Serial.print(F("PH Calib : "));
      
      Serial.print(pl.date);
      Serial.print(",");
      printPadding(pl.date, 10);

      Serial.print(ID_PH);
      Serial.print(",");
      printPadding(pl.ph_calibration.ph4, 14);
      
      Serial.print(pl.ph_calibration.ph4);
      Serial.print(",");
      printPadding(pl.ph_calibration.ph4, 14);
      
      Serial.print(pl.ph_calibration.ph7);
    }
    Serial.println();
  }
}


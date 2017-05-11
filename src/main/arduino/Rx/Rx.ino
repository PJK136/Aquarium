#include  <SPI.h>
#include  <RF24.h>
#include  "packet.h"

RF24 radio(9,10); 
payload_t pl;

unsigned long temps=micros();

int ID_LUM =0;
int ID_PH =1;
int ID_FLOW=2;
int ID_LVL=3;
int ID_TEMP=4;

//Affichage des données simplifié

void printSerialMeasure(int id, int value) {
  
  Serial.print(id);
  Serial.print(" ; ");
  Serial.print(value);
  Serial.print(",");
  int i =0;
  while(value!=0){
    i++;
    value=value/10;
  }
  for (int j = 0;j<(10-i);j++){
    Serial.print(" ");
  }
}

void setup() {
  Serial.begin(115200);    // Initialiser la communication série 
  Serial.println(F("Debut de l'ecoute..."));
  
  radio.begin();
  radio.setChannel(CHANNEL);
  radio.setDataRate(RF24_2MBPS);
  radio.enableDynamicPayloads();
  radio.setPALevel(RF24_PA_MAX);
  radio.setRetries(15,15);            //On met un délai de 4ms et un nombre max de tentatives de 15
  
  radio.openReadingPipe(0, ADDRESS); // Ouvrir le Pipe en lecture
  radio.startListening();
}

void loop(void) {
  if (Serial.available()) {
    char r = Serial.read();
    if (r == '0') {
      radio.stopListening();
      Serial.println("Arret");
    }
    else if (r == '1') {
      radio.startListening();
      Serial.println("Ecoute");
    }
  }

  while (radio.available()) 
  {
    /*
    //Format des Données :
    radio.read(&pl, sizeof(payload_t));
    //ID de l'envoyeur
    //Serial.print(F("Id envoyeur : "));
    Serial.print(pl.id);
    Serial.print(",");
    //Température mesurée
    //Serial.print(F("  ; Temperature: "));
    printSerialMeasure
    Serial.print(pl.temp);
    Serial.print(",");
    //Niveau d'eau mesuré
    //Serial.print(F("  ; Niveau: "));
    Serial.print(pl.level);
    Serial.print(",");
    //Débit mesuré
    //Serial.print(F("  ; Debit: "));
    Serial.print(pl.flow);
    Serial.print(",");
    Serial.print(F("  ; pH: "));
    Serial.print(pl.pH);
    Serial.print(F("  ; Luminosite: "));
    Serial.print(pl.lum);
    Serial.print(F("  ; Numero envoi: "));
    Serial.println(pl.date);
    Serial.print(F("  ; Temps: "));
    */
    radio.read(&pl, sizeof(payload_t));
    Serial.print(pl.date);
    if(pl.date<10){
      Serial.print(" ");
    }
    Serial.print(",    ");
    printSerialMeasure(ID_LUM,pl.lum);
    printSerialMeasure(ID_PH,pl.pH);
    printSerialMeasure(ID_FLOW,pl.flow);
    printSerialMeasure(ID_LVL,pl.level);
    printSerialMeasure(ID_TEMP,pl.temp);
    Serial.println();
  }
}


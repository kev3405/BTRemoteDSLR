//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Definitionen für physikalischen Zugriff
int focus = 9;
int shoot = 8;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Variablen und Konstanten zum Informationsaustausch

uint8_t vReceive[3];

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// interne Bearbeitung von Aufträgen
uint8_t time_interval; //Zeitintervall zwischen 2 Bildern
uint16_t pictureTaken; // Anzahl der durchgeführten Auslösungen
uint16_t pictureCount; // Gesamtanzahl der durch zu führenden Auslösungen


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


void setup() {
    //Initialisierung von Softwarestrukturen
    time_interval = 0;
    pictureTaken = 0;
    pictureCount = 0;
  
    //Schnittstelle zur DSLR
    pinMode(focus, OUTPUT);
    pinMode(shoot, OUTPUT);
    digitalWrite(focus, LOW);
    digitalWrite(shoot, LOW);
  
    //Schnittstelle zum Bluetooth Modul
    Serial.begin(9600);    
}

void loop() {
  //sind 3 Bytes empfangen?
  while(Serial.available() < 3);
  
  //Daten lesen
  vReceive[0] = Serial.read();  
  vReceive[1] = Serial.read();
  vReceive[2] = Serial.read();

  time_interval = vReceive[0];
  pictureCount = (((uint16_t) vReceive[1]) << 8) | ((uint16_t) vReceive[2]);
  
  Serial.print("Time Intervall: "); Serial.println(time_interval, DEC);
  Serial.print("Picture Count: "); Serial.println(pictureCount, DEC);
  
  //antworten!
  Serial.print(0xFF); 

  //Daten verarbeiten ... in dieser Zeit werden keine neuen Daten empfangen!!
  for(uint16_t i = 0; i < pictureCount; i++)
  {
    Serial.print("shoot ... "); Serial.println(i, DEC);
    digitalWrite(shoot, HIGH);
    delay(500);
    digitalWrite(shoot, LOW);
    delay(500);
    for(int j = 0; j < time_interval - 1 ; j++)
    {
      delay(1000);
    }
  }  
}

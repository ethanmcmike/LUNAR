#include <LoRa.h>
#include <SD.h>
#include <Servo.h>
#include <SoftwareSerial.h>
#include <SPI.h>
#include <TinyGPS++.h>

#define PIN_ALTIMETER 4
#define PIN_GPS_TX    3
#define PIN_GPS_RX    5
#define PIN_LORA_TX   0
#define PIN_LORA_RX   1
#define PIN_SD        7
#define PIN_DROGUE    8
#define PIN_CHUTE     9

TinyGPSPlus gpsParser;
SoftwareSerial gps(PIN_GPS_RX, PIN_GPS_TX);
SoftwareSerial lora(PIN_LORA_RX, PIN_LORA_TX);
File file;

void setup() {

  //Initialize pins
  pinMode(PIN_ALTIMETER,  INPUT);
  pinMode(PIN_SD,         OUTPUT);
  pinMode(PIN_DROGUE,     OUTPUT);
  pinMode(PIN_CHUTE,      OUTPUT);

  //Initialize communication
  Serial.begin(9600);
  gps.begin(115200);
  lora.begin(115200);

  //Initialize SD card
  SD.begin(PIN_SD);
  file = SD.open("log.txt", FILE_WRITE);
}

void loop() {

  //Measure altitude
  int alt = 0;
  
  //Read location
  double lat, lon;
  
  gpsParser.encode(gps.read());

  if(gpsParser.location.isValid()){
    lat = gpsParser.location.lat();
    lon = gpsParser.location.lng();
  }

  //Read time
  uint8_t hour, min, sec, csec;
  
  if(gpsParser.time.isValid()){
    hour = gpsParser.time.hour();
    min = gpsParser.time.minute();
    sec = gpsParser.time.second();
    csec = gpsParser.time.centisecond();
  }
  
  //Store time, altitude, location on SD
  if(file){
    file.print(hour);
    file.print(":");
    file.print(min);
    file.print(":");
    file.print(sec);
    file.print("\t");
    file.print(lat);
    file.print("\t");
    file.println(lon);
  }

  //Send location over radio
  LoRa.beginPacket();
  LoRa.print(lat);
  LoRa.print(lon);
  LoRa.endPacket();

  //Trigger separation
  if(false){
  
  }
  
  //Trigger drogue 'chute
  if(false){
    
  }
  
  //Trigger main 'chute
  if(false){
    
  }
}

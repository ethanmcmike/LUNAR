#include <Adafruit_GPS.h>
#include <LoRa.h>
#include <SD.h>
#include <Servo.h>
#include <SoftwareSerial.h>
#include <SPI.h>

#define PIN_LORA_RX     0
#define PIN_LORA_TX     1
#define PIN_GPS_TX      2
#define PIN_GPS_RX      3
#define PIN_SD          4
#define PIN_DROGUE      5
#define PIN_BODY        6
#define PIN_CHUTE       7
#define PIN_PAYLOAD     8
#define PIN_ALT         A5


SoftwareSerial lora(PIN_LORA_RX, PIN_LORA_TX);
SoftwareSerial gpsSerial(PIN_GPS_RX, PIN_GPS_TX);
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

  //Initialize gps
  gps.begin(9600);
  gps.sendCommand(PMTK_SET_NMEA_OUTPUT_RMCGGA);
  gps.sendCommand(PMTK_SET_NMEA_UPDATE_1HZ);   // 1 Hz update rate
  gps.sendCommand(PGCMD_ANTENNA);

  //Initialize radio
  LoRa.setPins(PIN_LORA_SS, PIN_LORA_RST, PIN_LORA_DI);
  LoRa.setSPI(0);
  LoRa.setSPIFrequency(0);
  LoRa.begin();

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

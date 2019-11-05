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

#define START           "["
#define STOP            "]"
#define DEL             ","

#define RECEIVER_ID     2

SoftwareSerial gps(PIN_GPS_RX, PIN_GPS_TX);
File file;

void setup() {

  //Initialize pins
  pinMode(PIN_SD,         OUTPUT);
  pinMode(PIN_DROGUE,     OUTPUT);
  pinMode(PIN_BODY,       OUTPUT);
  pinMode(PIN_CHUTE,      OUTPUT);
  pinMode(PIN_PAYLOAD,    OUTPUT);
  pinMode(PIN_ALT,        INPUT);

  //Initialize radio
  Serial.begin(9600);
  delay(1000);
  Serial.println("AT+IPR=9600");
  Serial.println("AT+PARAMETER=10,7,1,7");
  
  //Initialize gps
  gps.begin(115200);

  //Initialize SD card
  SD.begin(PIN_SD);
  file = SD.open("log.txt", FILE_WRITE);
}

void loop() {

  //Measure temperature
  float temp = 0;

  //Measure altitude
  int alt = 0;
  
  //Read location
  float lat = 0;
  float lon = 0;

  //Read time
  uint8_t hour, min, sec, csec;
  
//  if(gpsParser.time.isValid()){
//    hour = gpsParser.time.hour();
//    min = gpsParser.time.minute();
//    sec = gpsParser.time.second();
//    csec = gpsParser.time.centisecond();
//  }
  
  //Store time, altitude, location on SD
  if(file){
    file.print(hour);
    file.print(":");
    file.print(min);
    file.print(":");
    file.print(sec);
    file.print("\t");
    file.print(temp);
    file.print("\t");
    file.println(alt);
    file.print("\t");
    file.print(lat);
    file.print("\t");
    file.println(lon);
  }

  //Send data over radio
  sendData(RECEIVER_ID, temp, alt, lat, lon);
}

//Sends data over radio to specified receiver
void sendData(int receiverId, float temp, int alt, float lat, float lon){
  
  String data = START;
  data += DEL;
  data += String(temp, 1);
  data += DEL;
  data += String(alt);
  data += DEL;
  data += String(lat, 5);
  data += DEL;
  data += String(lon, 5);
  data += STOP;

  String msg = "AT+SEND=";
  data += receiverId;
  data += DEL;
  data += data.length();
  data += DEL;
  data += data;

  Serial.println(msg);
}

boolean apogee(){
  return false;
}

//Will deploy drogue parachute
void triggerDrogue(){
  digitalWrite(PIN_DROGUE, HIGH);
}

//Will separate rocket into two halves
void triggerBody(){
  digitalWrite(PIN_BODY, HIGH);
}

//Will deploy main parachute
void triggerMain(){
  digitalWrite(PIN_CHUTE, HIGH);
}

//Will activate a solenoid to release payload
void triggerPayload(){
  digitalWrite(PIN_PAYLOAD, HIGH);
}

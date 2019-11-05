#include <bmp085.h>
//#include <SD.h>
//#include <Servo.h>
#include <SoftwareSerial.h>
//#include <SPI.h>

#define LOG_FILE_NAME   "Log.txt"       //TODO: change file name to date of record

#define PIN_LORA_RX     0
#define PIN_LORA_TX     1
#define PIN_GPS_TX      2
#define PIN_GPS_RX      3
#define PIN_SD          4
#define PIN_DROGUE      5
#define PIN_BODY        6
#define PIN_CHUTE       7
#define PIN_PAYLOAD     8
#define PIN_BMP_SDA     A4
#define PIN_BMP_SCL     A5

#define DEL             ';'

#define RECEIVER_ID     1
#define RATE            2     //Transmissions per second [Hz]

#define COMMAND_KEY     "+RCV="
#define COMMAND_DROGUE  0
#define COMMAND_BODY    1
#define COMMAND_MAIN    2
#define COMMAND_PAYLOAD 3
int index, delCount, dataSize;
String buffer;

//File file;

#define ALT_NUM         20            //Number of altimeter data points to average
#define P0              101510.0      //Pressure at sea-level [Pa]
int alt;
long lastTransmit;

void setup() {

  //Initialize pins
  pinMode(PIN_SD,         OUTPUT);
  pinMode(PIN_DROGUE,     OUTPUT);
  pinMode(PIN_BODY,       OUTPUT);
  pinMode(PIN_CHUTE,      OUTPUT);
  pinMode(PIN_PAYLOAD,    OUTPUT);

  //Allow radio and gps to power up
  delay(1000);

  //Initialize radio
  Serial.begin(9600);
  Serial.println("AT+IPR=9600");
  delay(1000);
  Serial.println("AT+PARAMETER=10,7,1,7");

  //Initialize BMP (barometer/thermometer/accelerometer)
  bmp085_init();    //TODO: fix blocking when BMP085 not connected
  
  //Initialize gps

  //Initialize SD card
//  SD.begin(PIN_SD);
//  file = SD.open(LOG_FILE_NAME, FILE_WRITE);
}

void loop() {

  //Measure temperature
  float temp = bmp085Temp();

  //Measure altitude
  long p = bmp085Pressure();
  float tempAlt = bmp085PascalToMeter(p, P0);
  alt = ((ALT_NUM-1)*alt + tempAlt) / ALT_NUM;
  
  //Read location
  float lat = 0;
  float lon = 0;

  //Read time
  int hour = 0;
  int min = 0;
  int sec = 0;
  int msec = 0;
  
  //Store time, altitude, location on SD
  storeData(hour, min, sec, msec, temp, alt, lat, lon);

  //Listen for commands over radio
  while(Serial.available()){
    receiveData();
  }

  //Send data over radio
  long now = millis();
  if(now - lastTransmit > (float)1000/RATE){
    lastTransmit = now;
    sendData(RECEIVER_ID, temp, alt, lat, lon);
  }
}

//Example data: "+RCV=1,1,3,-99,40";
//Passes to handle(): "3"
void receiveData(){

  char c = Serial.read();

  //Validating data with key
  if(index < strlen(COMMAND_KEY)){
    
    if(c == COMMAND_KEY[index]){
      index++;
    }
  
    else{
        reset();
    }
  }

  //Already received key
  else {

    if(c == ','){
      
      delCount++;

      //Read transmitter id
      if(delCount == 1){
//        transmitterId = buffer.toInt();
        buffer = "";
      }
  
      //Read data size
      if(delCount == 2){
        dataSize = buffer.toInt();
        buffer = "";
      }

      //Translate data into a command
      if(delCount == 3){
        
        if(buffer.length() == dataSize){          
          handleData(buffer);
          reset();
        }
        
        else {
          reset();
        }
      }
    }
    
    //Add character to buffer
    else if(buffer.length() <= 256){
      if(isDigit(c) || c == DEL || c == '.') {
          buffer += c;
      }
    }

    else {
      reset();
    }
  }
}

void reset(){
  buffer = "";
  index = 0;
  delCount = 0;
  dataSize = 0;

  digitalWrite(PIN_BODY, LOW);
}

//Will handle commands received from ground
void handleData(String data){

  int commandId = data.toInt();

  switch(commandId){
    case COMMAND_DROGUE:
      triggerDrogue();
      break;

    case COMMAND_BODY:
      triggerBody();
      break;

    case COMMAND_MAIN:
      triggerMain();
      break;

    case COMMAND_PAYLOAD:
      triggerPayload();
      break;

    default:
      break;
  }
}

//Sends data over radio to specified receiver
void sendData(int receiverId, float temp, int alt, float lat, float lon){
  
  String data;
  data += String(temp, 1);
  data += DEL;
  data += String(alt);
  data += DEL;
  data += String(lat, 5);
  data += DEL;
  data += String(lon, 5);

  String msg = "AT+SEND=";
  msg += receiverId;
  msg += ',';
  msg += data.length();
  msg += ',';
  msg += data;

  Serial.println(msg);
}

void storeData(int hour, int min, int sec, int msec, float temp, int alt, float lat, float lon){
//  if(file){
//    file.print(hour);
//    file.print(":");
//    file.print(min);
//    file.print(":");
//    file.print(sec);
//    file.print("\t");
//    file.print(temp);
//    file.print("\t");
//    file.println(alt);
//    file.print("\t");
//    file.print(lat);
//    file.print("\t");
//    file.println(lon);
//  }
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

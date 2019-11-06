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

#define DEL_LORA        ','
#define DEL_LUNAR       ';'

#define RECEIVER_ID     1
#define RECORD_RATE     2     //Records per second [Hz]

#define COMMAND_KEY       "+RCV="
#define COMMAND_DROGUE    0
#define COMMAND_BODY      1
#define COMMAND_MAIN      2
#define COMMAND_PAYLOAD   3
#define COMMAND_TRANSMIT  4

int index, delCountLora, delCountLunar, dataSize;
String buffer;

//File file;

#define ALT_NUM         20            //Number of altimeter data points to average
#define P0              101510.0      //Pressure at sea-level [Pa]

int alt;
float temp, lat, lon;
long lastUpdate;

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
  temp = bmp085Temp();

  //Measure altitude
  long p = bmp085Pressure();
  float tempAlt = bmp085PascalToMeter(p, P0);
  alt = ((ALT_NUM-1)*alt + tempAlt) / ALT_NUM;
  
  //Read location
  lat = 0;
  lon = 0;

  //Read time
  int hour = 0;
  int min = 0;
  int sec = 0;
  int msec = 0;
  
  //Store time, altitude, location on SD
//  long now = millis();
//  if(now - lastTransmit > (float)1000/RATE){
//    lastUpdate = now;
//    storeData(hour, min, sec, msec, temp, alt, lat, lon);
//  }
  
  //Listen for commands over radio
  while(Serial.available()){
    receiveData();
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

    if(c == DEL_LORA){
      
      delCountLora++;

      //Read transmitter id
      if(delCountLora == 1){
//        transmitterId = buffer.toInt();
        buffer = "";
      }
  
      //Read data size
      if(delCountLora == 2){
        dataSize = buffer.toInt();
        buffer = "";
      }

      //Translate data into a command
      if(delCountLora == 3){        
        if(buffer.length() == dataSize){          
          handleData(buffer, delCountLunar+1);
          reset();
        }
        
        else {
          reset();
        }
      }
    }

    else if(c == DEL_LUNAR){
      delCountLunar++;
      buffer += c;
    }
    
    //Add character to buffer
    else if(buffer.length() <= 256){
      if(isDigit(c) || c == DEL_LUNAR || c == '.') {
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
  delCountLora = 0;
  delCountLunar = 0;
  dataSize = 0;
}

//Will handle commands received from ground
void handleData(String data, int numChunks){

  //Split data into chunks
  String chunks[numChunks];

  int index = 0;
  
  for(int i=0; i<numChunks; i++){
    
    char c = data[index++];
    
    while(c != DEL_LUNAR && index <= data.length()){      
      chunks[i] += c;
      c = data[index++];
    }
  }

  //First chunk is command id
  int commandId = chunks[0].toInt();

  //Execute command based on id
  switch(commandId){

    case COMMAND_TRANSMIT:
      if(numChunks == 1){
        sendData(RECEIVER_ID, temp, alt, lat, lon);
      }
      break;
    
    case COMMAND_DROGUE:
      if(numChunks == 2){
        boolean state = chunks[1].equals("1");
        triggerDrogue(state);
      }
      break;

    case COMMAND_BODY:
      if(numChunks == 2){
        boolean state = chunks[1].equals("1");
        triggerBody(state);
      }
      break;

    case COMMAND_MAIN:
      if(numChunks == 2){
        boolean state = chunks[1].equals("1");
        triggerMain(state);
      }
      break;

    case COMMAND_PAYLOAD:
      if(numChunks == 2){
        boolean state = chunks[1].equals("1");
        triggerPayload(state);
      }
      break;

    default:
      break;
  }
}

//Sends data over radio to specified receiver
void sendData(int receiverId, float temp, int alt, float lat, float lon){
  
  String data;
  data += String(temp, 1);
  data += DEL_LUNAR;
  data += String(alt);
  data += DEL_LUNAR;
  data += String(lat, 5);
  data += DEL_LUNAR;
  data += String(lon, 5);

  String msg = "AT+SEND=";
  msg += receiverId;
  msg += DEL_LORA;
  msg += data.length();
  msg += DEL_LORA;
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
void triggerDrogue(boolean state){
  digitalWrite(PIN_DROGUE, state);
}

//Will separate rocket into two halves
void triggerBody(boolean state){
  digitalWrite(PIN_BODY, state);
}

//Will deploy main parachute
void triggerMain(boolean state){
  digitalWrite(PIN_CHUTE, state);
}

//Will activate a solenoid to release payload
void triggerPayload(boolean state){
  digitalWrite(PIN_PAYLOAD, state);
}

#include <bmp085.h>
#include <Parser.h>
#include <SD.h>
#include <SmartBuffer.h>
#include <SoftwareSerial.h>
//#include <SPI.h>

//TODO: change file names to include date of record
#define LOG_FILE_NAME   "Log.txt"       //Records flight events (parachute deployment, commands received...)
#define DATA_FILE_NAME  "Data.txt"      //Records flight data (altitude, temperature, acceleration...)

#define PIN_LORA_RX       0
#define PIN_LORA_TX       1
#define PIN_LED_TX        9
#define PIN_SD            4
#define PIN_DROGUE        5
#define PIN_BODY          6
#define PIN_CHUTE         7
#define PIN_PAYLOAD       8
#define PIN_GPS_TX        10
#define PIN_GPS_RX        11
#define PIN_BMP_SDA       A4
#define PIN_BMP_SCL       A5

#define BT_TX       2
#define BT_RX       3

#define DEL_LORA          ','
#define DEL_LUNAR         ';'

#define RECEIVER_ADDR     1
#define ROCKET_ADDR       2

#define RECORD_RATE       2     //Rate to store data on SD card [Hz]
  
#define ID_COMMAND        0
#define ID_STATUS         1

#define KEY_RCV          "+RCV="
#define KEY_OK           "+OK"

#define COMMAND_DROGUE    0
#define COMMAND_BODY      1
#define COMMAND_MAIN      2
#define COMMAND_PAYLOAD   3
#define COMMAND_TRANSMIT  4

enum Command{
  STATUS,
  TRIGGER_DROGUE,
  TRIGGER_MAIN,
  TRIGGER_BODY,
  TRIGGER_PAYLOAD
};

SoftwareSerial gpsSerial(PIN_GPS_TX, PIN_GPS_RX);
SoftwareSerial bt(BT_TX, BT_RX);

File logFile, dataFile;

#define ALT_NUM         20            //Number of altimeter data points to average
#define P0              102010.0      //Pressure at sea-level [Pa]

int alt;
float temp, lat, lon;
long lastUpdate, lastMove, lastStore;
int hour, min, sec, msec;

//GPS data
char ch = "";
String str = "";
String targetStr = "GPGGA";

//OK Buffer
SmartBuffer okBuffer(KEY_OK), rcvBuffer(KEY_RCV);
boolean sending;
boolean commandReady;

int size = 5;
String buffer[5];
int slotSize[5] = {1, 3, 1000, 4, 1};
void handle(String* data, int size);
Parser parser(KEY_RCV, 5, buffer, ',', slotSize, handle);

long txTime;
int timeout = 5000;

boolean waiting, msgReady;

void setup() {

  //Initialize pins
  pinMode(PIN_SD,         OUTPUT);
  pinMode(PIN_DROGUE,     OUTPUT);
  pinMode(PIN_BODY,       OUTPUT);
  pinMode(PIN_CHUTE,      OUTPUT);
  pinMode(PIN_PAYLOAD,    OUTPUT);
  pinMode(PIN_LED_TX,     OUTPUT);

  //Setup bluetooth for debugging
  bt.begin(9600);

  //Allow radio and gps to power up
  delay(1000);

  //Initialize radio
  Serial.begin(9600);
  Serial.println("AT+PARAMETER=10,7,1,7");    //Short range, fast
//  Serial.println("AT+PARAMETER=12,5,1,10");      //Long range, slow
  delay(10);
  Serial.println("AT+ADDRESS=" + String(ROCKET_ADDR));

  //Initialize BMP (barometer/thermometer/accelerometer)
  bmp085_init();    //TODO: fix blocking when BMP085 not connected
  
  //Initialize gps
  gpsSerial.begin(9600);

//  //Initialize SD card
//  SD.begin(PIN_SD);
//  logFile = SD.open(LOG_FILE_NAME, FILE_WRITE);
//  dataFile = SD.open(DATA_FILE_NAME, FILE_WRITE);
}

void loop() {

  long now = millis();

  //Read sensor input
  readTemp();
  readAltitude();
  readLocation();
  readAcceleration();

//  //Store data on SD card
//  if(now - lastStore >= 1000.0 / RECORD_RATE){
//    lastStore = now;
//    storeData();
//  }

  //Trigger events
  
//  if(now - lastTransmit > (float)1000/RATE){
//    lastUpdate = now;
//    storeData(hour, min, sec, msec, temp, alt, lat, lon);
//  }
  
  //Listen for commands over radio
  while(Serial.available()){

    char c = Serial.read();

    okBuffer.put(c);

    if(okBuffer.full() || now - txTime > timeout){
      okBuffer.reset();
      waiting = false;
      txTime = now;
      digitalWrite(PIN_LED_TX, LOW);
    }

    bt.print(c);

    //Respond to data
    parser.put(c);
  }

  if(msgReady && !Serial.available()){
    sendData(RECEIVER_ADDR);
//    Serial.println("AT+SEND=1,5,abcde");
    waiting = true;
    msgReady = false;
    digitalWrite(PIN_LED_TX, HIGH);
  }
}

void readTemp(){
  temp = bmp085Temp();
}

void readAltitude(){
  long p = bmp085Pressure();
  float tempAlt = bmp085PascalToMeter(p, P0);
  alt = ((ALT_NUM-1)*alt + tempAlt) / ALT_NUM;
}

void readLocation(){
  
  if(gpsSerial.available()){
    
    ch = gpsSerial.read();

    if(ch == '\n'){

      if(targetStr.equals(str.substring(1, 6))){
        
        int first = str.indexOf(",");
        int two = str.indexOf(",", first+1);
        int three = str.indexOf(",", two+1);
        int four = str.indexOf(",", three+1);
        int five = str.indexOf(",", four+1);
        
        String Lat = str.substring(two+1, three);
        String Long = str.substring(four+1, five);

        String Lat1 = Lat.substring(0, 2);
        String Lat2 = Lat.substring(2);

        String Long1 = Long.substring(0, 3);
        String Long2 = Long.substring(3);

        double LatF = Lat1.toDouble() + Lat2.toDouble()/60;
        float LongF = Long1.toFloat() + Long2.toFloat()/60;

        //Assumes North/West hemisphere
        lat = LatF;
        lon = -LongF;
        
      }
      str = "";
    }else{
      str += ch;
    }
  }
}

void readAcceleration(){
  
}

//Stores flight data on SD card
//Returns true if record was successful
boolean storeData(){
  
  if(dataFile){
    dataFile.print(getTime());
    dataFile.print("\t");
    dataFile.print(temp);
    dataFile.print("\t");
    dataFile.print(alt);
    dataFile.print("\t");
    dataFile.print(lat);
    dataFile.print("\t");
    dataFile.println(lon);

    //Ensure data is saved to SD
    dataFile.flush();

    return true;
  }

  return false;
}

//Converts hour, min, sec, msec to a readable string
//eg 10:34:53.913
String getTime(){
  
  String time = "";
  time += String(hour);
  time += ":";
  time += String(min);
  time += ":";
  time += String(sec);
  time += ".";
  time += String(msec);

  return time;
}

void resetOk(){
  okBuffer.reset();
  sending = false;
}

void reset(){
  parser.reset();
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
        sendData(RECEIVER_ADDR);
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
void sendData(int receiverId){

  sending = true;
  
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

//Stores flight events on SD card
void log(String msg){
  if(logFile){
    logFile.print(getTime());
    logFile.print("\t");
    logFile.println(msg);

    //Ensure data is saved to SD
    logFile.flush();
  }
}

boolean apogee(){
  return false;
}

//Will deploy drogue parachute
void triggerDrogue(boolean state){
  log("Drogue parachute deployed");
  digitalWrite(PIN_DROGUE, state);
}

//Will separate rocket into two halves
void triggerBody(boolean state){
  log("Body split");
  digitalWrite(PIN_BODY, state);
}

//Will deploy main parachute
void triggerMain(boolean state){
  log("Main parachute deployed");
  digitalWrite(PIN_CHUTE, state);
}

//Will activate a solenoid to release payload
void triggerPayload(boolean state){
  log("Payload released");
  digitalWrite(PIN_PAYLOAD, state);
}

void handle(String* data, int size){
  if(!waiting){
    msgReady = true;
  }
}

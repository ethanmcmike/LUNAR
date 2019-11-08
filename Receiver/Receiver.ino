#include <SoftwareSerial.h>
//#include <Parser.h>

#define LORA_TX     0
#define LORA_RX     1
#define BT_TX       2
#define BT_RX       3

#define KEY         "+RCV="
#define START       '['
#define STOP        ']'
#define DEL_LORA    ','
#define DEL_LUNAR   ';'

#define COMMAND_DROGUE    0
#define COMMAND_BODY      1
#define COMMAND_MAIN      2
#define COMMAND_PAYLOAD   3
#define COMMAND_PING      4

#define ROCKET_ID   2

#define PING_RATE   2   //Rate to request data from rocket [Hz]
#define TIMEOUT     2000
long now, lastUpdate;

//Flags
String command;
boolean commandReady, waiting;

SoftwareSerial bt(BT_TX, BT_RX);

int index, delCount, transmitterId, dataSize;
String buffer, bufferBT;
int indexBT;

void setup() {
  
  Serial.begin(9600);
  bt.begin(9600);

  //Initialize LoRa
  delay(1000);
  Serial.println("AT+IPR=9600");
  delay(1000);
  Serial.println("AT+PARAMETER=10,7,1,7");
}

void loop() {

  //Receive data from rocket
  if(Serial.available()){
    parseRocket(Serial.read());
  }

  //Receive command from Android
  if(bt.available()){
    parseBT(bt.read());
  }

  now = millis();

  //Send rocket a command
  if(!waiting || now - lastUpdate > 2000){
    
    if(commandReady){
      sendRocket(command);
      commandReady = false;
    }
  
    //Send to rocket if available and not already waiting for response
    else{
      lastUpdate = now;
      waiting = true;
      sendRocket("4");
    }
  }
}

//Parses data from the rocket given a single byte at a time
//Input format: Address, Data Size, Temperature, Altitude, Latitude, Longitude
//Output format: Temperature, Altitude, Latitude, Longitude
//Example data: "+RCV=2,27,13.2;203;172.11451;12.21451,-99,40";
//Passes to handle(): "13.2;203;172.11451;12.21451"
void parseRocket(char c){

  //Validating data with key
  if(index < strlen(KEY)){
    
    if(c == KEY[index]){
      index++;
    }
  
    else{
        reset();
    }
  }

  //Already received key
  else {

    if(c == DEL_LORA){
      
      delCount++;

      //Read transmitter id
      if(delCount == 1){
        transmitterId = buffer.toInt();
        buffer = "";
      }
  
      //Read data size
      if(delCount == 2){
        dataSize = buffer.toInt();
        buffer = "";
      }

      //Send data through bluetooth
      if(delCount == 3){
        if(buffer.length() == dataSize){
          sendBT(buffer);
          waiting = false;
          reset();
        }
        
        else {
          reset();
        }
      }
    }
    
    //Add character to buffer
    else if(buffer.length() <= 256){
      if(isDigit(c) || c == DEL_LUNAR || c == '.' || c == '-') {
        buffer += c;
      }
    }

    else {
      reset();
    }
  }
}

//Parses data from the Android given a single byte at a time
void parseBT(char c){

  if(c == START){
    bufferBT = "";
  }

  else if(c == STOP){
    command = bufferBT;
    commandReady = true;
    bufferBT = "";
  }

  else if(bufferBT.length() <= 256){
    if(isDigit(c) || c == DEL_LUNAR || c == '.'){
      bufferBT += c;
    }
  }
}

//Data input format: "temp;alt;lat;lon"
//Example data input: "13.2;203;172.11451;12.21451"
//Data output format: "[trandmitterId;temp;alt;lat;lon]"
//Example data output: "[2;13.2;203;172.11451;12.21451]"
void sendBT(String data){
  
  String msg = String(START);
  msg += transmitterId;
  msg += DEL_LUNAR;
  msg += data;
  msg += STOP;
  
  bt.println(msg);
}

void sendRocket(String data){
  
  String msg = "AT+SEND=";
  msg += ROCKET_ID;
  msg += DEL_LORA;
  msg += String(data.length());
  msg += DEL_LORA;
  msg += data;
  
  Serial.println(msg);
}

void reset(){
  buffer = "";
  index = 0;
  delCount = 0;
  dataSize = 0;
}

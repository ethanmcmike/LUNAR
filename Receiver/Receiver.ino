#include <SoftwareSerial.h>

#define LORA_TX     0
#define LORA_RX     1
#define BT_TX       2
#define BT_RX       3

#define KEY         "+RCV="
#define START       '['
#define STOP        ']'
#define DEL         ';'

SoftwareSerial bt(BT_TX, BT_RX);

int index, delCount, transmitterId, dataSize;
String buffer;

void setup() {
  
  Serial.begin(9600);
  bt.begin(115200);

  //Initialize LoRa
  delay(1000);
  Serial.println("AT+IPR=9600");
  Serial.println("AT+PARAMETER=10,7,1,7");
}

void loop() {
  if(Serial.available()){
    parseData(Serial.read());
  }
}

//Example data: "+RCV=2,27,13.2;203;172.11451;12.21451,-99,40";
//Passes to handle(): "13.2;203;172.11451;12.21451"
void parseData(char c){

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

    if(c == ','){
      
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
          sendData(buffer);
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

//Data input format: "temp;alt;lat;lon"
//Example data input: "13.2;203;172.11451;12.21451"
//Data output format: "[trandmitterId;temp;alt;lat;lon]"
//Example data output: "[2;13.2;203;172.11451;12.21451]"
void sendData(String data){
  
  String msg = String(START);
  msg += transmitterId;
  msg += DEL;
  msg += data;
  msg += STOP;
  
  bt.println(msg);
}

void reset(){
  buffer = "";
  index = 0;
  delCount = 0;
  dataSize = 0;
}

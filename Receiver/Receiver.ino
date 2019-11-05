#include <SoftwareSerial.h>

#define LORA_TX   0
#define LORA_RX   1
#define BT_TX     2
#define BT_RX     3

#define KEY       "+RCV="
#define START     "["
#define STOP      "]"
#define DEL_IN    ","
#define DEL_OUT   ";"

SoftwareSerial bt(BT_TX, BT_RX);

int index;
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

void parseData(char c){

  Serial.println(c);
  
  if(c == KEY[index]){
    index++;
  }

  else if(c == START){
    
  }
  
  else if(c == STOP){
      handleData(buffer);
      buffer = "";
  }
  else if(buffer.length() <= 256){

      if(isDigit(c) || c == DEL_IN || c == '.') {
          buffer += c;
      }
  }
  else{
      buffer = "";
  }
}

void handleData(String data){
  Serial.println(data);
}

void sendData(float temp, int alt, float lat, float lon){
  String data = START;
  data += String(temp, 1);
  data += DEL_OUT;
  data += String(alt);
  data += DEL_OUT;
  data += String(lat, 5);
  data += DEL_OUT;
  data += String(lon, 5);
  data += STOP;

  bt.println(data);
}

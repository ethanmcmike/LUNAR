#include <Parser.h>
#include <SoftwareSerial.h>
#include <SmartBuffer.h>
#include <Timeout.h>

//Pins
#define PIN_LORA_TX     0
#define PIN_LORA_RX     1
#define PIN_BT_TX       2
#define PIN_BT_RX       3
#define PIN_LED_TX      4

//Command IDs
#define COMMAND_DROGUE    0
#define COMMAND_BODY      1
#define COMMAND_MAIN      2
#define COMMAND_PAYLOAD   3
#define COMMAND_PING      4

//Radio addresses
#define RECEIVER_ADDR     1
#define ROCKET_ADDR       2

#define PING_RATE   2         //Rate to request data from rocket [Hz]
#define TIMEOUT     5000

//Parsing
#define KEY_OK                "+OK"
#define KEY_SEND              "AT+SEND="
#define KEY_RCV               "+RCV="
#define KEY_START             '['
#define KEY_STOP              ']'
#define DEL                   ';'
#define DEL_LORA              ','
#define BUFFER_SIZE_ROCKET    5
#define BUFFER_SIZE_BT        5

//Function declarations
void receiveRocket(String*, int);
void receiveBt(String*, int);

//Buffers
SmartBuffer okBuffer(KEY_OK);

String rocketBuffer[BUFFER_SIZE_ROCKET];
int rocketSlots[BUFFER_SIZE_ROCKET] = {1, 3, 1000, 4, 1};
Parser rocketParser(KEY_RCV, BUFFER_SIZE_ROCKET, rocketBuffer, DEL_LORA, rocketSlots, receiveRocket);

String btBuffer[BUFFER_SIZE_ROCKET];
int btSlots[BUFFER_SIZE_ROCKET] = {1, 3, 1000, 4, 1};
Parser btParser(KEY_START, BUFFER_SIZE_BT, btBuffer, DEL, btSlots, receiveBt);

//Timing
Timeout timeout(TIMEOUT);

//Flags
String msg, command;
boolean msgReady, commandReady, waiting;

SoftwareSerial bt(PIN_BT_TX, PIN_BT_RX);

//Stats testing
int numTimeouts;
int numTransmit;

void setup() {

  //Initialize pins
  pinMode(PIN_LED_TX, OUTPUT);

  //Initialize communication
  Serial.begin(9600);
  bt.begin(9600);

  //Allow radio/bluetooth to power up
  delay(1000);

  //Initialize LoRa
  Serial.println("AT+PARAMETER=10,7,1,7");    //Short range, fast
//  Serial.println("AT+PARAMETER=12,5,1,10");      //Long range, slow
  delay(10);
  Serial.println("AT+ADDRESS=" + String(RECEIVER_ADDR));
}

void loop() {

  long now = millis();

  //Send rocket data request as quickly as possible
  if((!waiting && okBuffer.full()) || timeout.expired()){    

    //Stats
    if(timeout.expired()){
      numTimeouts++;
    }

    numTransmit++;

    String stats = "Timed-out: ";
    stats += String(numTimeouts);
    stats += " out of ";
    stats += String(numTransmit);
    bt.println(stats);

    
    Serial.println("AT+SEND=2,1,4");
//    bt.println(timeout.get());
    timeout.reset();
    okBuffer.reset();
    waiting = true;
    digitalWrite(PIN_LED_TX, HIGH);
  }

  //Listen to radio input
  while(Serial.available()){

    char c = Serial.read();

//    bt.print(c);

    //Listen for KEY_OK after send
    okBuffer.put(c);

    if(okBuffer.full()){
      digitalWrite(PIN_LED_TX, LOW);
    }

    //Listen for data
    rocketParser.put(c);
  }

  //Receive command from Android
  if(bt.available()){
    
//    parseBT(bt.read());
  }

  //Send data
  if(!Serial.available()){

    //Send message to bluetooth
    if(msgReady){
      
      sendBT(msg);

      waiting = false;
      msgReady = false;
    }

    //Send command to rocket
    if(commandReady){

      commandReady = false;
    }
  }
}

////Parses data from the Android given a single byte at a time
//void parseBT(char c){
//
//  if(c == START){
//    bufferBT = "";
//  }
//
//  else if(c == STOP){
//    command = bufferBT;
//    commandReady = true;
//    bufferBT = "";
//  }
//
//  else if(bufferBT.length() <= 256){
//    if(isDigit(c) || c == DEL_LUNAR || c == '.'){
//      bufferBT += c;
//    }
//  }
//}

void receiveRocket(String* in, int size){
  
  if(size >= BUFFER_SIZE_ROCKET){
    
    String addr = in[0];
    int dataSize = in[1].toInt();
    String data = in[2];
    
    msg = data;

    msgReady = true;
  }
}

void receiveBt(String* in, int size){
  
  if(size >= BUFFER_SIZE_BT){
    
    String addr = in[0];
    String dataSize = in[1];
    String data = in[2];

    command = String(KEY_SEND);
    command += ROCKET_ADDR;
    command += DEL_LORA;
    command += dataSize;
    command += DEL_LORA;
    command += data;
    
    commandReady = true;
  }
}

//Data input format: "temp;alt;lat;lon"
//Example data input: "13.2;203;172.11451;12.21451"
//Data output format: "[trandmitterId;temp;alt;lat;lon]"
//Example data output: "[2;13.2;203;172.11451;12.21451]"
void sendBT(String data){
  
  String msg = String(KEY_START);
  msg += ROCKET_ADDR;             //TODO - remove ADDR from both here and Android code
  msg += DEL;
  msg += data;
  msg += KEY_STOP;
  bt.println(msg);
}

void sendRocket(String data){
  
  String msg = "AT+SEND=";
  msg += ROCKET_ADDR;
  msg += DEL_LORA;
  msg += String(data.length());
  msg += DEL_LORA;
  msg += data;
  
  Serial.println(msg);
}

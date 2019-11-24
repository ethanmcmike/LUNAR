#include <ParserC.h>
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

#define PING_RATE         2         //Rate to request data from rocket [Hz]
#define TIMEOUT           3000

//Parsing
#define KEY_OK                "+OK"
#define KEY_SEND              "AT+SEND="
#define KEY_RCV               "+RCV="
#define KEY_START             "["
#define KEY_STOP              "]"
#define DEL                   ';'
#define DEL_LORA              ','
#define BUFFER_SIZE_ROCKET    5
#define BUFFER_SIZE_BT        2

//Function declarations
void receiveRocket(char**, int, int*);
void receiveBt(char**, int, int*);

//Buffers
SmartBuffer okBuffer(KEY_OK);

int rocketSlots[BUFFER_SIZE_ROCKET] = {1, 3, 100, 4, 1};
ParserC rocketParser(KEY_RCV, BUFFER_SIZE_ROCKET, DEL_LORA, rocketSlots, receiveRocket);

int btSlots[BUFFER_SIZE_BT] = {1, 1};
ParserC btParser(KEY_START, BUFFER_SIZE_BT, DEL, btSlots, receiveBt);

//Timing
Timeout timeout(TIMEOUT);

//Flags
boolean waiting;
char command;           //TODO - change to queue of commands
boolean commandReady;   //TODO - Will become !commandQueueEmpty

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
//  delay(10);
//  Serial.println("AT+ADDRESS=" + String(RECEIVER_ADDR));
}

void loop() {

  long now = millis();

  //First priority:
  //Listen for bluetooth data
  if(bt.available()){

    byte c = bt.read();
    
    btParser.put(c);
  }

  //Listen to radio data
  if(Serial.available()){

    char c = Serial.read();

    //Listen for KEY_OK after send
    okBuffer.put(c);

    if(okBuffer.full()){
      digitalWrite(PIN_LED_TX, LOW);
    }

    //Listen for data from rocket
    rocketParser.put(c);
  }

  //Set command to first in command queue
  //If no command from BT, send data request command
  command = commandReady ? command : '4';
  
  //Send rocket commands as quickly as possible
  if((!waiting && okBuffer.full()) || timeout.expired()){

    //////Stats
    if(timeout.expired()){
      numTimeouts++;
    }

    numTransmit++;

    String stats = "Timed-out: ";
    stats += String(numTimeouts);
    stats += " out of ";
    stats += String(numTransmit);
    bt.println(stats);
    bt.println((long)timeout.get());
    ///////

    digitalWrite(PIN_LED_TX, HIGH);
    
    sendRocket(String(command));

    okBuffer.reset();
    timeout.reset();
    waiting = true;
  }
}

void receiveRocket(char** in, int size, int* sizes){
  
  if(size >= BUFFER_SIZE_ROCKET){
    
    String addr = String((char*)in[0]);
    int dataSize = sizes[2];
    char* data = (char*)in[2];

    //Assuming addr is rocket address..

    //Command feedback
    if(dataSize == 1){
      
      if(command == data[0]){
        waiting = false;
        commandReady = false;
      }
    }

    //Data received
    else{

      //Send data to bluetooth
      int state = data[0];
      int alt = *((int*)((byte*)(&data[1])));
      float lat = *((float*)((byte*)(&data[3])));
      float lon = *((float*)((byte*)(&data[7])));

      String msg = "";
      msg += String(23.4);    //TODO - remove temp from format
      msg += DEL;
      msg += String(alt);
      msg += DEL;
      msg += String(lat, 7);
      msg += DEL;
      msg += String(lon, 7);

      sendBt(msg);
      
      waiting = false;
    }
  }
}

void receiveBt(char** in, int size, int* sizes){

//  for(int i=0; i<size; i++){
//    char* chunk = in[i];
//
//    for(int j=0; j<sizes[i]; j++){
//      bt.print(chunk[j]);
//    }
//    bt.println();
//  }
  
  if(size >= BUFFER_SIZE_BT){
    char* chunk = in[0];
    command = chunk[0];         //Command ID
    commandReady = true;
  }
}

//Data input format: "temp;alt;lat;lon"
//Example data input: "13.2;203;172.11451;12.21451"
//Data output format: "[trandmitterId;temp;alt;lat;lon]"
//Example data output: "[2;13.2;203;172.11451;12.21451]"
void sendBt(String data){
  
  String msg = String(KEY_START);
  msg += ROCKET_ADDR;             //TODO - remove ADDR from both here and Android code
  msg += DEL;
  msg += data;
  msg += KEY_STOP;

  bt.println(msg);
}

void sendRocket(String data){
  
  String cmd = String(KEY_SEND);
  cmd += ROCKET_ADDR;
  cmd += DEL_LORA;
  cmd += String(data.length());
  cmd += DEL_LORA;
  cmd += data;
  Serial.println(cmd);
}

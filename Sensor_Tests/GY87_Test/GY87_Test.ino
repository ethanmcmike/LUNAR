#include <bmp085.h>
#include <Wire.h>

//PIN SCL -> A5
//PIN SDA -> A4

#define P0 101510.0       //Pressure at sea-level [Pa]
#define ALT_NUM 100       //Number of altimeter data points to average

float altBuffer[ALT_NUM];
int altIndex;

void setup(){  
  
  Serial.begin(9600);
  bmp085_init();

  //Fill altimeter buffer with initial values
  for(int i=0; i<ALT_NUM; i++){
    bmp085Temp();
    altBuffer[i] = bmp085PascalToMeter(bmp085Pressure(), P0);
  }
}

void loop(){

  //Read temp./press.
  float temp = bmp085Temp();
  long p = bmp085Pressure();
  
  //Add new pressure to altimeter buffer
  float alt = bmp085PascalToMeter(p, P0);
  altBuffer[altIndex] = alt;

  //Loop altimeter buffer
  altIndex++;
  if(altIndex >= ALT_NUM)
     altIndex = 0;

  //Get average height of altimeter buffer
  alt = 0;

  for(int i=0; i<ALT_NUM; i++){
    alt += altBuffer[i];
  }

  alt = alt / ALT_NUM;

  Serial.print("Alt: ");
  Serial.print(alt);
  Serial.print(" m\t");
  Serial.print("Temp: ");
  Serial.print(temp);
  Serial.println(" C");
}

#include <bmp085.h>

//PIN SCL -> A5
//PIN SDA -> A4

#define P0 101510.0       //Pressure at sea-level [Pa]
#define ALT_NUM 20       //Number of altimeter data points to average

float alt;

void setup(){  
  
  Serial.begin(9600);
  bmp085_init();

  //Set initial altitude
  bmp085Temp();
  alt = bmp085PascalToMeter(bmp085Pressure(), P0);
}

void loop(){

  //Read temp./press.
  float temp = bmp085Temp();
  long p = bmp085Pressure();
  
  //Calculate altitude
  float tempAlt = bmp085PascalToMeter(p, P0);
  //Modify altitude based on weight average
  alt = ((ALT_NUM-1)*alt + tempAlt) / ALT_NUM;

  //Comment-out either Output #1 or #2
  //Output #1
  //Monitor altitude/temperature in Serial Monitor
  Serial.print("Altitude: ");
  Serial.print(alt, 1);
  Serial.print(" [m]\t");
  Serial.print("Temperature: ");
  Serial.print(temp, 1);
  Serial.println(" [C]");

//  //Output #2
//  //Monitor average altitude vs instantaneous on Serial Plotter
//  Serial.print(tempAlt);
//  Serial.print("\t");
//  Serial.println(alt);
}

#include <SPI.h>
#include <SD.h>

#define CHIP_SELECT   4
#define FILE_NAME     "log.txt"

//Test data
int hour = 11;
int min = 34;
int sec = 13;
int msec = 873;
float alt = 8122.31;
float lat = 32.1943;
float lon = 94.1421;

void setup() {
  
  Serial.begin(9600);

  Serial.print("Initializing SD card...");
  if (!SD.begin(CHIP_SELECT)) {
    Serial.println("Card failed, or not present");
    while (1);
  }
  Serial.println("card initialized.");
}

void loop() {

  File file = SD.open(FILE_NAME, FILE_WRITE);

  if (file) {
    file.print(hour);
    file.print(":");
    file.print(min);
    file.print(":");
    file.print(sec);
    file.print(":");
    file.print(msec);
    file.print("\t");
    file.print(lat);
    file.print("\t");
    file.println(lon);
    file.close();
  }
  
  else {
    Serial.println("error opening datalog.txt");
  }
}

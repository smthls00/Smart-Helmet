#include <SPI.h>
#include "FS.h"
#include "SD.h"
#include "SPI.h"
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include "Adafruit_BME680.h"
#include "RTClib.h"
#include "Adafruit_CCS811.h"
#include "Adafruit_VEML6070.h"

#define SD_CS_PIN 33 //File: "pins_arduino.h" Slave Select
#define SEALEVELPRESSURE_HPA (1013.25)

File myFile;

Adafruit_BME680 bme;
Adafruit_CCS811 ccs;
Adafruit_VEML6070 veml6070;
RTC_PCF8523 rtc;
DateTime oldTime;

char daysOfTheWeek[7][12] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

char filename[] = "/dataLogs.txt";

void bme680Init() {
  if (!bme.begin()) {
    Serial.println("Could not find a valid BME680 sensor, check wiring!");
    while (1);
  }

  // Set up oversampling and filter initialization
  bme.setTemperatureOversampling(BME680_OS_8X);
  bme.setHumidityOversampling(BME680_OS_2X);
  bme.setPressureOversampling(BME680_OS_4X);
  bme.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme.setGasHeater(320, 150); // 320*C for 150 ms
}

void bme680Read() {

  if (! bme.performReading()) {
    Serial.println("Failed to perform reading :(");
    return;
  }
  //  Serial.print("Temperature = ");
  //  Serial.print(bme.temperature);
  //  Serial.println(" *C");
  //
  //  Serial.print("Pressure = ");
  //  Serial.print(bme.pressure / 100.0);
  //  Serial.println(" hPa");
  //
  //  Serial.print("Humidity = ");
  //  Serial.print(bme.humidity);
  //  Serial.println(" %");
  //
  //  Serial.print("Gas = ");
  //  Serial.print(bme.gas_resistance / 1000.0);
  //  Serial.println(" KOhms");
  //
  //  Serial.print("Approx. Altitude = ");
  //  Serial.print(bme.readAltitude(SEALEVELPRESSURE_HPA));
  //  Serial.println(" m");
  //
  //  Serial.println();
}


void rtcInit() {
  if (!rtc.begin()) {
    Serial.println("Couldn't find RTC");
    while (1);
  }

  if (!rtc.initialized()) {
    Serial.println("RTC is NOT running!");
    // following line sets the RTC to the date & time this sketch was compiled
    rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
  }
}


void sdInit() {
  if (!SD.begin(SD_CS_PIN)) {
    Serial.println("SD card Initialization failed!");
    while (1);
  }


  //remove file
//  SD.remove(filename);
//  while(1);
}

void ccs811Init() {
  if (!ccs.begin()) {
    Serial.println("Failed to start sensor! Please check your wiring.");
    while (1);
  }

  // Wait for the sensor to be ready
  while (!ccs.available());
}

void ccs811Read() {
  if (ccs.available()) {
    if (!ccs.readData()) {
      //      Serial.print("CO2: ");
      //      Serial.print(ccs.geteCO2());
      //      Serial.print("ppm, TVOC: ");
      //      Serial.println(ccs.getTVOC());
    }
    else {
      Serial.println("ERROR!");
      while (1);
    }
  }
}


void appendLogs() {
  DateTime now = rtc.now();

  if (abs(now.minute() - oldTime.minute()) >= 1) {

    if (SD.exists(filename)) {

      myFile = SD.open(filename, FILE_APPEND);

      if (myFile) {

        String timeLog = "\n\n\n" + String(now.year()) + "-" + String(now.month()) + "-" + String(now.day()) + "-" + String(daysOfTheWeek[now.dayOfTheWeek()]) + "-" + String(now.hour()) + "-" + String(now.minute()) + "-" + String(now.hour()) + "\n";

        String bme680Log = String("BME680\n") + "Temperature: " + String(bme.temperature) + " *C, " + "Pressure: " + String(bme.pressure / 100.0) + " hPa, " + "Humidity: " + String(bme.humidity) + " %, " + "Gas: " + String(bme.gas_resistance / 1000.0) + " Kohms, " + "Altitude: " + bme.readAltitude(SEALEVELPRESSURE_HPA) + " m\n";
        String ccs811Log = String("CCS811\n") + "CO2: " + String(ccs.geteCO2()) + " ppm, " + "TVOC: " + String(ccs.getTVOC()) + " ppm\n";
        String veml6070Log = String("VEML6070\n") + "UV: " + String(veml6070.readUV()) + "\n";
        String overallLog = timeLog + bme680Log + ccs811Log + veml6070Log;


        Serial.println(overallLog);

        myFile.println(overallLog);
        myFile.close();

        oldTime = now;
      } else {
        // if the file didn't open, print an error:
        Serial.println("error opening");
      }
    }
    else {
      Serial.println("doesn't exist, let's create");
      myFile = SD.open(filename, FILE_WRITE);
      myFile.close();
    }
  }
}


void setup() {
  Serial.begin(115200);

  pinMode(33, OUTPUT);
  digitalWrite(33, HIGH);

  Serial.println("Initialization");

  rtcInit();
  sdInit();
  bme680Init();
  ccs811Init();
  veml6070.begin(VEML6070_1_T);

  for (int i = 0; i < 5; i++) {
    bme680Read();
    ccs811Read();
    veml6070.readUV();
  }

  appendLogs();


  Serial.println("Initialization done");
}

void loop() {
  bme680Read();
  ccs811Read();
  veml6070.readUV();
  appendLogs();
}

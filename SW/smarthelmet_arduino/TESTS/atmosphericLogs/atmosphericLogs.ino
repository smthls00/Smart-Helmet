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
#define SEALEVELPRESSURE_HPA (1021) // Kaiserslautern 20th June

#define LED_PIN 13

File myFile;

Adafruit_BME680 bme;
Adafruit_CCS811 ccs;
Adafruit_VEML6070 veml6070;
RTC_PCF8523 rtc;
DateTime oldTime;

char daysOfTheWeek[7][12] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

String fileName;

long logMillis;
long ledMillis;

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

  DateTime now = rtc.now();
  fileName = "/" + String(now.year()) + '-' + String(now.month()) + '-' + String(now.day()) + '-' + String(now.hour()) + '-' + String(now.minute()) + '-' + String(now.second()) + ".txt";

  Serial.println(fileName);

  myFile = SD.open(fileName, FILE_WRITE);
  myFile.close();

  if (SD.exists(fileName)) {
    Serial.println("file exists");
  } else {
    Serial.println("file doesn't exist");
    while (1);
  }
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
  if (millis() - logMillis >= 60000) {

    myFile = SD.open(fileName, FILE_APPEND);


    if (myFile) {

      //        String timeLog = "\n\n\n" +  + "\n";
      //        String bme680Log = String("BME680\n") + "Temperature: " + String(bme.temperature) + " *C, " + "Pressure: " + String(bme.pressure / 100.0) + " hPa, " + "Humidity: " + String(bme.humidity) + " %, " + "Gas: " + String(bme.gas_resistance / 1000.0) + " Kohms, " + "Altitude: " + bme.readAltitude(SEALEVELPRESSURE_HPA) + " m\n";
      //        String ccs811Log = String("CCS811\n") + "CO2: " + String(ccs.geteCO2()) + " ppm, " + "TVOC: " + String(ccs.getTVOC()) + " ppm\n";
      //        String veml6070Log = String("VEML6070\n") + "UV: " + String(veml6070.readUV()) + "\n";

      String timeLog = String(millis()) + ", ";

      String bme680Log = String(bme.temperature) + ", " + String(bme.pressure / 100.0) + ", " + String(bme.humidity) + ", " + String(bme.gas_resistance / 1000.0) + ", " + bme.readAltitude(SEALEVELPRESSURE_HPA) + ", ";
      String ccs811Log = String(ccs.geteCO2()) + ", " + String(ccs.getTVOC()) + ", ";
      String veml6070Log = String(veml6070.readUV());


      String overallLog = timeLog + bme680Log + ccs811Log + veml6070Log;

      Serial.println(overallLog);

      myFile.println(overallLog);
      myFile.close();

      logMillis = millis();

    } else {
      Serial.println("failed to open");
    }
  }
}


void setup() {
  Serial.begin(115200);

  pinMode(SD_CS_PIN, OUTPUT);
  digitalWrite(SD_CS_PIN, HIGH);

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

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

  ledMillis = millis();
}

void loop() {
  bme680Read();
  ccs811Read();
  veml6070.readUV();
  appendLogs();


  if (millis() - ledMillis >= 1500) {
    ledMillis = millis();
    digitalWrite(LED_PIN, !digitalRead(LED_PIN));
  }
}

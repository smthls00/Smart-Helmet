/*

   Smart helmet code for Teensy 4.0




   I2C Sensors: BNO055, BME680, MAX30102, CCS811


   Author: Anar Aliyev




*/

#include <Wire.h>
#include "MAX30105.h"
#include "heartRate.h"
#include "Adafruit_CCS811.h"
#include <Adafruit_BNO055.h>
#include <Adafruit_BME680.h>

#define SEALEVELPRESSURE_HPA (1013.25)
#define LED_PIN 13

Adafruit_BNO055 bno = Adafruit_BNO055(55, 0x28);
Adafruit_CCS811 ccs;
Adafruit_BME680 bme;
MAX30105 particleSensor;


/*
   NTC calculations
*/
#define THERMISTORFOREHEAD A0
#define THERMISTOREAR A1
// resistance at 25 degrees C
#define THERMISTORNOMINAL 10000
// temp. for nominal resistance (almost always 25 C)
#define TEMPERATURENOMINAL 25
// how many samples to take and average, more takes longer
// but is more 'smooth'
#define NUMSAMPLES 5
// The beta coefficient of the thermistor (usually 3000-4000)
#define BCOEFFICIENT 3950
// the value of the 'other' resistor
#define SERIESRESISTOR 10000

int samples[NUMSAMPLES];

const byte RATE_SIZE = 4; //Increase this for more averaging. 4 is good.
byte rates[RATE_SIZE]; //Array of heart rates
byte rateSpot = 0;
long lastBeat = 0; //Time at which the last beat occurred

float beatsPerMinute;
int beatAvg;
float tmpAvg;
byte k = 0;

uint16_t eco2;
uint16_t tvoc;
/*
   MAX30102 Init
*/
void max30102_init() {

  Serial.println("MAX30102 Init");

  // Initialize sensor
  if (!particleSensor.begin(Wire)) //Use default I2C port, 400kHz speed
  {
    Serial.println("MAX30102 Fail");
    while (1);
  }
  //Serial.println("Place your index finger on the sensor with steady pressure.");

  particleSensor.setup(); //Configure sensor with default settings
  particleSensor.setPulseAmplitudeRed(0x0A); //Turn Red LED to low to indicate sensor is running
  particleSensor.setPulseAmplitudeGreen(0); //Turn off Green LED
}


/*
   MAX30102 Read
*/
void max30102_read() {

  long irValue = particleSensor.getIR();

  if (checkForBeat(irValue) == true)
  {
    //We sensed a beat!
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20)
    {
      rates[rateSpot++] = (byte)beatsPerMinute; //Store this reading in the array
      rateSpot %= RATE_SIZE; //Wrap variable

      //Take average of readings
      beatAvg = 0;
      for (byte x = 0 ; x < RATE_SIZE ; x++)
        beatAvg += rates[x];
      beatAvg /= RATE_SIZE;
    }


    if (k++ >= 5) {
      k = 5;
      Serial1.print("Y");
      Serial1.print(beatAvg);
      Serial1.print("\n");
    }

    //Serial.println("beat");
  }
}



/*
   CCS811 Init
*/
void ccs811_init() {

  Serial.println("CCS811 Init");

  if (!ccs.begin()) {
    Serial.println("CCS811 Fail");
    while (1);
  }

  // Wait for the sensor to be ready
  while (!ccs.available());

  Serial.println("CCS811 Done");
}


/*
   CCS811 Read
*/
void ccs811_read() {
  ccs.readData();
  //  if (ccs.available()) {
  //
  //
  //    if (!ccs.readData()) {
  //
  //      eco2 = ccs.geteCO2();
  //      tvoc = ccs.getTVOC();
  //      //Serial.print("CO2: ");
  //      //Serial.print(ccs.geteCO2());
  //      //Serial.print("ppm, TVOC: ");
  //      //Serial.println(ccs.getTVOC());
  //    }
  //    else {
  //      Serial.println("ERROR!");
  //      while (1);
  //    }
  //  }
}


/*
   BNO055 Init
*/
void bno055_init() {
  Serial.println("BNO055 Init");

  if (!bno.begin())
  {
    /* There was a problem detecting the BNO055 ... check your connections */
    Serial.println("BNO055 Fail");
    while (1);
  }

  Serial.println("BNO055 Done");
}


/*
   BNO055 Sensor Read
*/
void bno055_read() {
  sensors_event_t orientationData , angVelocityData , linearAccelData;
  bno.getEvent(&orientationData, Adafruit_BNO055::VECTOR_EULER);
  //bno.getEvent(&angVelocityData, Adafruit_BNO055::VECTOR_GYROSCOPE);
  //bno.getEvent(&linearAccelData, Adafruit_BNO055::VECTOR_LINEARACCEL);

  printEvent(&orientationData);
  //printEvent(&angVelocityData);
  //printEvent(&linearAccelData);


  //  int8_t boardTemp = bno.getTemp();
  //  Serial.print(F("temperature: "));
  //  Serial.println(boardTemp);
}


/*
   BNO055 Printing
*/
void printEvent(sensors_event_t* event) {
  Serial.println();
  Serial.print(event->type);
  double x = -1000000, y = -1000000 , z = -1000000; //dumb values, easy to spot problem
  if (event->type == SENSOR_TYPE_ACCELEROMETER) {
    x = event->acceleration.x;
    y = event->acceleration.y;
    z = event->acceleration.z;
  }
  else if (event->type == SENSOR_TYPE_ORIENTATION) {
    x = event->orientation.x;
    y = event->orientation.y;
    z = event->orientation.z;
  }
  else if (event->type == SENSOR_TYPE_MAGNETIC_FIELD) {
    x = event->magnetic.x;
    y = event->magnetic.y;
    z = event->magnetic.z;
  }
  else if ((event->type == SENSOR_TYPE_GYROSCOPE) || (event->type == SENSOR_TYPE_ROTATION_VECTOR)) {
    x = event->gyro.x;
    y = event->gyro.y;
    z = event->gyro.z;
  }

  Serial.print(": x= ");
  Serial.print(x);
  Serial.print(" | y= ");
  Serial.print(y);
  Serial.print(" | z= ");
  Serial.println(z);
}

/*
   BME680 Init
*/
void bme680_init() {
  Serial.println("BME680 Init");

  if (!bme.begin()) {
    Serial.println("BME680 Fail");
    while (1);
  }

  // Set up oversampling and filter initialization
  bme.setTemperatureOversampling(BME680_OS_8X);
  bme.setHumidityOversampling(BME680_OS_2X);
  bme.setPressureOversampling(BME680_OS_4X);
  bme.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme.setGasHeater(320, 150); // 320*C for 150 ms

  Serial.println("BME680 Done");
}


/*
   BME680 Read
*/
void bme680_read() {
  //Serial.println("BME680");

  if (!bme.performReading()) {
    Serial.println("BME680 Fail");
    while (1);
  }
  //    Serial.print("TMP = ");
  //    Serial.print(bme.temperature);
  //    Serial.println(" *C");
  //
  //    Serial.print("PRS = ");
  //    Serial.print(bme.pressure / 100.0);
  //    Serial.println(" hPa");
  //
  //    Serial.print("HMT = ");
  //    Serial.print(bme.humidity);
  //    Serial.println(" %");
  //
  //    Serial.print("GAS = ");
  //    Serial.print(bme.gas_resistance / 1000.0);
  //    Serial.println(" KOhms");
  //
  //    Serial.print("ALT = ");
  //    Serial.print(bme.readAltitude(SEALEVELPRESSURE_HPA));
  //    Serial.println(" m");


  //  if (SerialBT.available() && (millis() - bmeMillis >= 1)) {
  //
  //    if (!bme.performReading()) {
  //      Serial.println("BME680 Fail");
  //      while (1);
  //    }
  //
  //
  //    char bmeBT[255];
  //    sprintf(bmeBT, "t%.1fp%.1fh%.1fg%.1fa%.1f", bme.temperature, (bme.pressure / 100.0), bme.humidity, (bme.gas_resistance / 1000.0), bme.readAltitude(SEALEVELPRESSURE_HPA));
  //    SerialBT.write((uint8_t*)bmeBT, strlen(bmeBT));
  //
  //    bmeMillis = millis();
  //  }
}


/*
   NTC Read
*/
float NTC_read(byte THERMISTORPIN) {

  uint8_t i;
  float average;

  // take N samples in a row, with a slight delay
  for (i = 0; i < NUMSAMPLES; i++) {
    samples[i] = analogRead(THERMISTORPIN);
    delay(10);
  }

  // average all the samples out
  average = 0;
  for (i = 0; i < NUMSAMPLES; i++) {
    average += samples[i];
  }
  average /= NUMSAMPLES;

  //Serial.print("Average analog reading ");
  //Serial.println(average);

  // convert the value to resistance
  average = 1023 / average - 1;
  average = SERIESRESISTOR / average;
  //Serial.print("Thermistor resistance ");
  //Serial.println(average);

  float steinhart;
  steinhart = average / THERMISTORNOMINAL;     // (R/Ro)
  steinhart = log(steinhart);                  // ln(R/Ro)
  steinhart /= BCOEFFICIENT;                   // 1/B * ln(R/Ro)
  steinhart += 1.0 / (TEMPERATURENOMINAL + 273.15); // + (1/To)
  steinhart = 1.0 / steinhart;                 // Invert
  steinhart -= 273.15;                         // convert to C

  //Serial.print("Temperature ");
  //Serial.print(steinhart);
  //Serial.println(" *C");

  return steinhart;
}

/*
   NTC Read
*/
void NTC_average() {

  tmpAvg = (NTC_read(THERMISTORFOREHEAD) + NTC_read(THERMISTOREAR)) / 2.0;

  Serial.print(tmpAvg);
  Serial.println("*C");
}

void serial_write() {
  char str[128];

  ccs811_read();
  bme680_read();
  max30102_read();
  NTC_average();

  sprintf(str, "c%dv%do%.1fp%.1fh%.1fg%.1fa%.1fb%dt%.1f\n", ccs.geteCO2(), ccs.getTVOC(),
          bme.temperature, bme.pressure / 100.0, bme.humidity,
          bme.gas_resistance / 1000.0, bme.readAltitude(SEALEVELPRESSURE_HPA),
          beatAvg, tmpAvg);
  //sprintf(str, "hello\n");
  Serial1.write(str, strlen(str));
  Serial.println(str);
}

void calibration() {
  Serial.println("Calibration Start");


  for (int i = 0; i < 25; i++) {
    ccs811_read();
    bme680_read();
    max30102_read();
    NTC_average();
  }


  Serial.println("Calibration End");

}

void setup()
{
  Serial.begin(9600);
  Serial1.begin(115200);

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH);

  //while(!Serial.available());

  //Serial.println("Initializing...");

  analogReadResolution(10);

  ccs811_init();
  bme680_init();
  //bno055_init();
  max30102_init();

  calibration();
}

void loop()
{
  serial_write();
  delay(5);
  //ccs811_read();
  //delay(2000);
  //bno055_read();
  //delay(2000);
  //bme680_read();
  //delay(2000);
}

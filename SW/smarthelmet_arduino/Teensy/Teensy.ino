/*
   Smart helmet code for Teensy 4.0


   Sensors: MAX30102, 2x NTC


   Author: Anar Aliyev
*/


/*
   Includes
*/
#include <Wire.h>
#include "MAX30105.h"
#include "heartRate.h"
#include <Adafruit_BNO055.h>


/*
   Defines
*/
//#define DEBUG 1

//NTC calculations defines
#define THERMISTOR_FOREHEAD A0
#define THERMISTOR_EAR A1
#define THERMISTOR_NOMINAL 10000
#define TEMPERATURE_NOMINAL 25
#define NUM_SAMPLES 5
#define B_COEFFICIENT 3950
#define SERIES_RESISTOR 10000

#define LED_PIN 13


#define RATE_SIZE 4


/*
   Global variables
*/
int samples[NUM_SAMPLES];

byte rates[RATE_SIZE];
byte rateSpot = 0;
long lastBeat = 0;

float beatsPerMinute;
int beatAvg;
float tmpAvg;

char sensorsDataString[16];


/*
   Global sensors objects
*/
MAX30105 max30102Obj;
Adafruit_BNO055 bno055Obj = Adafruit_BNO055(55, 0x28);


/*
   MAX30102 Init
*/
void max30102Init() {
#ifdef DEBUG
  Serial.println("MAX30102 Init");
#endif

  if (!max30102Obj.begin()) //Use default I2C port, 400kHz speed
  {
#ifdef DEBUG
    Serial.println("MAX30102 Fail");
#endif
    while (1);
  }

  max30102Obj.setup(); //Configure sensor with default settings
  max30102Obj.setPulseAmplitudeRed(0x0A); //Turn Red LED to low to indicate sensor is running
  max30102Obj.setPulseAmplitudeGreen(0); //Turn off Green LED

#ifdef DEBUG
  Serial.println("MAX30102 Done");
#endif
}


/*
   MAX30102 Read
*/
void max30102Read() {

  long irValue = max30102Obj.getIR();

  if (checkForBeat(irValue) == true)
  {
    //We sensed a beat!
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20)
    {
      rates[rateSpot++] = (byte)beatsPerMinute;
      rateSpot %= RATE_SIZE;

      //Take average of readings
      beatAvg = 0;
      for (byte x = 0 ; x < RATE_SIZE ; x++)
        beatAvg += rates[x];
      beatAvg /= RATE_SIZE;
    }
  }
}


/*
   NTC Read
*/
float NTCRead(byte THERMISTOR_PIN) {

  uint8_t i;
  float average;

  // take N samples in a row, with a slight delay
  for (i = 0; i < NUM_SAMPLES; i++) {
    samples[i] = analogRead(THERMISTOR_PIN);
    //delay(10);
  }

  // average all the samples out
  average = 0;
  for (i = 0; i < NUM_SAMPLES; i++) {
    average += samples[i];
  }
  average /= NUM_SAMPLES;

  average = 1023 / average - 1;
  average = SERIES_RESISTOR / average;

  float steinhart;
  steinhart = average / THERMISTOR_NOMINAL;           // (R/Ro)
  steinhart = log(steinhart);                         // ln(R/Ro)
  steinhart /= B_COEFFICIENT;                         // 1/B * ln(R/Ro)
  steinhart += 1.0 / (TEMPERATURE_NOMINAL + 273.15);  // + (1/To)
  steinhart = 1.0 / steinhart;                        // Invert
  steinhart -= 273.15;                                // convert to C

  return steinhart;
}

/*
   NTC compute average between ear and forehead
*/
void NTCAverage() {

  float tmpForehead = NTCRead(THERMISTOR_FOREHEAD);
  float tmpEar = NTCRead(THERMISTOR_EAR);
  tmpAvg = (tmpForehead + tmpEar) / 2.0;

#ifdef DEBUG
  Serial.print("forehead: ");
  Serial.print(tmpForehead);
  Serial.println("*C");
  Serial.print("ear: ");
  Serial.print(tmpEar);
  Serial.println("*C");
  Serial.print("average: ");
  Serial.print(tmpAvg);
  Serial.println("*C");
#endif
}

/*
   BNO055 Init
*/
void bno055Init() {
#ifdef DEBUG
  Serial.println("BNO055 Init");
#endif

  if (!bno055Obj.begin())
  {
#ifdef DEBUG
    Serial.println("BNO055 Fail");
#endif
    while (1);
  }

#ifdef DEBUG
  Serial.println("BNO055 Done");
#endif
}


/*
   BNO055 Sensor Read
*/
void bno055Read() {
  sensors_event_t orientationData , angVelocityData , linearAccelData;
  bno055Obj.getEvent(&orientationData, Adafruit_BNO055::VECTOR_EULER);
  //bno.getEvent(&angVelocityData, Adafruit_BNO055::VECTOR_GYROSCOPE);
  //bno.getEvent(&linearAccelData, Adafruit_BNO055::VECTOR_LINEARACCEL);

  bno055Print(&orientationData);
  //printEvent(&angVelocityData);
  //printEvent(&linearAccelData);


  //  int8_t boardTemp = bno.getTemp();
  //  Serial.print(F("temperature: "));
  //  Serial.println(boardTemp);
}


/*
   BNO055 Print
*/
void bno055Print(sensors_event_t* event) {
#ifdef DEBUG
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
#endif
}


/*
   Sensors calibration
*/
void calibration() {
#ifdef DEBUG
  Serial.println("Calibration Start");
#endif

  for (int i = 0; i < 100; i++) {
    max30102Read();
    NTCAverage();
  }

#ifdef DEBUG
  Serial.println("Calibration End");
#endif
}


/*
   Initialization
*/
void setup()
{
#ifdef DEBUG
  Serial.begin(9600);
#endif

  Serial1.begin(9600);

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH);

  Wire.begin();

  analogReadResolution(10);

  max30102Init();
  bno055Init();

  calibration();
}


/*
   Main loop
*/
void loop()
{

  NTCAverage();

  for (byte i = 0; i < 150; i++)
    max30102Read();

  if (Serial1)
  {
    sprintf(sensorsDataString, "b%dt%.1f\n", beatAvg, tmpAvg);
    Serial1.write((uint8_t*)sensorsDataString, strlen(sensorsDataString));
#ifdef DEBUG
    Serial.println(sensorsDataString);
#endif

  }
}

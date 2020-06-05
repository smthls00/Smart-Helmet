/*

   Smart helmet code for Teensy 4.0




   I2C Sensors: MAX30102


   Author: Anar Aliyev




*/

#include <Wire.h>
#include "MAX30105.h"
#include "heartRate.h"
#include <Adafruit_BNO055.h>

#define LED_PIN 13

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
byte beatCount = 0;

char outputString[16];

MAX30105 particleSensor;
Adafruit_BNO055 bno = Adafruit_BNO055(55, 0x28);

/*
   MAX30102 Init
*/
void max30102_init() {

  Serial.println("MAX30102 Init");

  // Initialize sensor
  if (!particleSensor.begin()) //Use default I2C port, 400kHz speed
  {
    Serial.println("MAX30102 Fail");
    while (1);
  }
  //Serial.println("Place your index finger on the sensor with steady pressure.");

  particleSensor.setup(); //Configure sensor with default settings
  particleSensor.setPulseAmplitudeRed(0x0A); //Turn Red LED to low to indicate sensor is running
  particleSensor.setPulseAmplitudeGreen(0); //Turn off Green LED

  Serial.println("MAX30102 Done");
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


    //if (beatCount++ >= 5) {
    //  beatCount = 5;
//      Serial.print("beatAvg ");
//      Serial.println(beatAvg);
//      Serial.print("BPM ");
//      Serial.println(beatsPerMinute);
    //}

    //Serial.println("beat");
  }
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

  float forehead = NTC_read(THERMISTORFOREHEAD);
  float ear = NTC_read(THERMISTOREAR);
  tmpAvg = (forehead + ear) / 2.0;
  
  Serial.print("forehead: ");
  Serial.print(forehead);
  Serial.println("*C");
  Serial.print("ear: ");
  Serial.print(ear);
  Serial.println("*C");
  Serial.print("average: ");
  Serial.print(tmpAvg);
  Serial.println("*C");
}

/*
   BNO055 Init
*/
void bno055_init() {
  Serial.println("BNO055 Init");

  if (!bno.begin())
  {
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


void calibration() {
  Serial.println("Calibration Start");


  for (int i = 0; i < 100; i++) {
    max30102_read();
    NTC_average();
  }

//  while(beatCount != 5){
//    max30102_read();
//  }
  


  Serial.println("Calibration End");

}

void setup()
{
  Serial.begin(9600);
  Serial1.begin(9600);

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH);

  Wire.begin();

  max30102_init();
  bno055_init();

  analogReadResolution(10);

  calibration();
}

void loop()
{

  NTC_average();
    
  for(byte i = 0; i < 150; i++)
    max30102_read();

  if(Serial1)
  {

    sprintf(outputString, "b%dt%.1f\n", beatAvg, tmpAvg);
    Serial1.write((uint8_t*)outputString, strlen(outputString));
    Serial.println(outputString);
    
  }
  
}

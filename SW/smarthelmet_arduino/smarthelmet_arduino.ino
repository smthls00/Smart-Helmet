/*
 * 
 * Smart helmet code for HUZZAH32
 * 
 * 
 * 
 * MCU: ESP32/Huzzah Board
 * 
 * I2C Sensors: VCNL4040, BME680, VL53L0X, BNO055, MAX30102, TMP006
 * 
 * 
 * Author: Anar Aliyev
 * 
 * 
 * 
 * 
 */


/*
 * Includes
 */
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BME680.h>
#include <Adafruit_VL53L0X.h>
#include <Adafruit_VCNL4040.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BNO055.h>

#include <MAX30105.h>
#include <heartRate.h>

#include <BluetoothSerial.h>

#include <utility/imumaths.h>


/*
 * Defines
 */
#define SEALEVELPRESSURE_HPA (1013.25)
#define LONG_RANGE


/*
 * Global sensors objects
 */
Adafruit_BME680 bme;
Adafruit_VL53L0X lox;
Adafruit_VCNL4040 vcnl4040;
Adafruit_BNO055 bno = Adafruit_BNO055(55, 0x28);

BluetoothSerial SerialBT;

MAX30105 particleSensor;


/*
 * Global variables for MAX30102 Sensor
 */
const byte RATE_SIZE = 4; //Increase this for more averaging. 4 is good.
byte rates[RATE_SIZE]; //Array of heart rates
byte rateSpot = 0;
long lastBeat = 0; //Time at which the last beat occurred

float beatsPerMinute;
int beatAvg;


/*
 * BME680 Init
 */
void bme680_init(){
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
 * BME680 Read
 */
void bme680_read(){
  Serial.println("BME680");
  
  if (! bme.performReading()) {
    Serial.println("BME680 Fail");
    while (1);
  }
  Serial.print("TMP = ");
  Serial.print(bme.temperature);
  Serial.println(" *C");

  Serial.print("PRS = ");
  Serial.print(bme.pressure / 100.0);
  Serial.println(" hPa");

  Serial.print("HMT = ");
  Serial.print(bme.humidity);
  Serial.println(" %");

  Serial.print("GAS = ");
  Serial.print(bme.gas_resistance / 1000.0);
  Serial.println(" KOhms");

  Serial.print("ALT = ");
  Serial.print(bme.readAltitude(SEALEVELPRESSURE_HPA));
  Serial.println(" m");

}


/*
 * VL53L0X Init
 */
void vl53l0x_init(){
  Serial.println("VL53L0X Init");
  
  if (!lox.begin()) {
    Serial.println("VL53L0X Fail");
    while(1);
  }


  Serial.println("VL53L0X Done");
}


/*
 * VL53L0X Read
 */
void vl53l0x_read(){
  VL53L0X_RangingMeasurementData_t measure;
    
  Serial.println("VL53L0X");
  
  lox.rangingTest(&measure, false); // pass in 'true' to get debug data printout!

  if (measure.RangeStatus != 4) {  // phase failures have incorrect data
    Serial.print("Distance (mm): "); Serial.println(measure.RangeMilliMeter);
  } else {
    Serial.println(" out of range ");
  }
}


/*
 * VCNL4040 Init
 */
void vcnl4040_init(){
  Serial.println("VCNL4040 Init");
  
  if (!vcnl4040.begin()) {
    Serial.println("VCNL4040 Fail");
    while (1);
  }
  
  Serial.println("VCNL4040 Done");
}


/*
 * VCNL4040 Read
 */
void vcnl4040_read(){
  Serial.println("VCNL4040");
  
  Serial.print("Proximity:"); Serial.println(vcnl4040.getProximity());
  Serial.print("Ambient light:"); Serial.println(vcnl4040.getAmbientLight());
  Serial.print("White light:"); Serial.println(vcnl4040.getWhiteLight());  
}


/*
 * BNO055 Init
 */
void bno055_init(){
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
 * BNO055 Sensor Read
 */
void bno055_read(){
  sensors_event_t orientationData , angVelocityData , linearAccelData;
  bno.getEvent(&orientationData, Adafruit_BNO055::VECTOR_EULER);
  bno.getEvent(&angVelocityData, Adafruit_BNO055::VECTOR_GYROSCOPE);
  bno.getEvent(&linearAccelData, Adafruit_BNO055::VECTOR_LINEARACCEL);

  printEvent(&orientationData);
  printEvent(&angVelocityData);
  printEvent(&linearAccelData);


//  int8_t boardTemp = bno.getTemp();
//  Serial.print(F("temperature: "));
//  Serial.println(boardTemp);
}


/*
 * BNO055 Printing
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
 * MAX30102 Init
 */
void max30102_init(){
  Serial.println("MAX30102 Init");
  
  if (!particleSensor.begin(Wire)) //Use default I2C port, 400kHz speed
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
 * MAX30102 Read
 */
void max30102_read(){
  Serial.print("MAX30102");
  
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
  }

  Serial.print("IR=");
  Serial.print(irValue);
  Serial.print(", BPM=");
  Serial.print(beatsPerMinute);
  Serial.print(", Avg BPM=");
  Serial.print(beatAvg);

  if (irValue < 50000)
    Serial.print(" No touch!");

  Serial.println();
}


/*
 * Debugging
 */
void debug(){
  Serial.println("*******************************");
  delay(1000);
}


/*
 * Initialization
 */
void setup() {
  Serial.begin(115200);

   while (! Serial) {
    delay(1);
  }

  Serial.println("Initialization");

//  //bme680_init();
//  vl53l0x_init();
//  vcnl4040_init();
//  bno055_init();
//  max30102_init();

  SerialBT.begin("ESP32_SmartHelmet");

  delay(2000);
}


/*
 * Main Loop
 */
void loop() {
  
//  bme680_read();
//  debug();
//  
//  vl53l0x_read();
//  debug();
//
//  vcnl4040_read();
//  debug();
//
//  bno055_read();
//  debug();
//
//  max30102_read();
//  debug();

//if (Serial.available()) {
//    SerialBT.write(Serial.read());
//  }
//  if (SerialBT.available()) {
//    Serial.write(SerialBT.read());
//  }
if(SerialBT.available()){
    char str[] = "hello\n";
    for(int i = 0; i < strlen(str); i++)
      SerialBT.write(str[i]);
}

delay(5000);
  
}

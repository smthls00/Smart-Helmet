/*

   Smart helmet code for HUZZAH32



   MCU: ESP32/Huzzah Board

   I2C Sensors: VCNL4040, BME680, VL53L0X, BNO055


   Author: Anar Aliyev




*/


/*
   Includes
*/
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_VL53L0X.h>
#include <Adafruit_VCNL4040.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_VEML6070.h>
#include <Adafruit_NeoPixel.h>
#include <Adafruit_DRV2605.h>

#include <MAX30105.h>
#include <heartRate.h>

#include <BluetoothSerial.h>
#include <MQ2Lib.h>


/*
   Defines
*/
#define LONG_RANGE 1

#define NEOPIXELPIN        21
#define NUMPIXELS 8

#define MQ2_PIN A0
/*
   Global sensors objects
*/
Adafruit_VL53L0X lox;
Adafruit_VCNL4040 vcnl4040;
Adafruit_VEML6070 uv = Adafruit_VEML6070();
MQ2 mq2(MQ2_PIN, true); //instance (true=with serial output enabled)

Adafruit_DRV2605 drv;
Adafruit_NeoPixel pixels(NUMPIXELS, NEOPIXELPIN, NEO_GRB + NEO_KHZ800);

BluetoothSerial SerialBT;

MAX30105 particleSensor;

long ambientMillis = 0;
String inputString = "";         // a String to hold incoming data
bool stringComplete = false;  // whether the string is complete
float lpg = 0, co = 0, smoke = 0;
/*
   VL53L0X Init
*/
void vl53l0x_init() {
  Serial.println("VL53L0X Init");

  if (!lox.begin()) {
    Serial.println("VL53L0X Fail");
    while (1);
  }


  Serial.println("VL53L0X Done");
}


/*
   VL53L0X Read
*/
void vl53l0x_read() {
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
   VCNL4040 Init
*/
void vcnl4040_init() {
  Serial.println("VCNL4040 Init");

  if (!vcnl4040.begin()) {
    Serial.println("VCNL4040 Fail");
    while (1);
  }

  Serial.println("VCNL4040 Done");
}


/*
   VCNL4040 Read
*/
void vcnl4040_read() {
  Serial.println("VCNL4040");

  Serial.print("Proximity:"); Serial.println(vcnl4040.getProximity());
  Serial.print("Ambient light:"); Serial.println(vcnl4040.getAmbientLight());
  Serial.print("White light:"); Serial.println(vcnl4040.getWhiteLight());
}


/*
   VEML6070 Init
*/
void veml6070_init() {
  Serial.println("VEML6070 Init");
  uv.begin(VEML6070_1_T);  // pass in the integration time constant
}


/*
   VEML6070 Read
*/
void veml6070_read() {
  uint16_t ambientLight = uv.readUV();

  if (millis() - ambientMillis >= 500) {

    if (ambientLight == 0)
      neopixel_set(true);
    else
      neopixel_set(false);
  }
}


/*
   NEOPIXEL Init
*/
void neopixel_init() {
  pixels.begin();
  pixels.clear();


  pixels.show();
}


/*
   NEOPIXEL Set
*/
void neopixel_set(bool flag) {

  if (flag) {
    for (int i = 0; i < NUMPIXELS; i++)
      pixels.setPixelColor(i, pixels.Color(0, 0, 200));
  }
  else
    pixels.clear();


  pixels.show();
}

/*
   Read Incoming BT Data
*/
void bt_serial_read() {
  if (SerialBT.available()) {

    char c = SerialBT.read();
    if (c == 'C') {

      char command[10];
      byte k = 0;

      c = SerialBT.read();
      while (SerialBT.available() && c != 'X' && k < 10) {
        command[k++] = c;
        c = SerialBT.read();
      }

      Serial.print("string: ");
      Serial.println(command);

      if (command[0] == 'v') {
        drv2605l_set();
      }
    }
  }
}


/*
   DRV2605L Init
*/
void drv2605l_init() {
  Serial.println("DRV2605L Init");

  drv.begin();

  drv.selectLibrary(1);

  // I2C trigger by sending 'go' command
  // default, internal trigger when sending GO command
  drv.setMode(DRV2605_MODE_INTTRIG);
}


/*
   DRV2605L Set
*/
void drv2605l_set() {
  // set the effect to play
  drv.setWaveform(0, 16);  // play effect
  drv.setWaveform(1, 0);   // end waveform

  // play the effect!
  drv.go();
}


/*
   Debugging
*/
void debug() {
  Serial.println("*******************************");
  delay(1000);
}


/*
   Read Serial
*/
void serial_read() {
  if (Serial1.available()) {

    char c = Serial1.read();
    Serial.print("char: ");
    Serial.println(c);

    if (c == 'Y') {

      char bpmVal[10];
      byte k = 0;

      c = Serial1.read();
      while (Serial1.available() && c != '\n' && k < 10) {
        bpmVal[k++] = c;

        c = Serial1.read();
      }

      Serial.print("string: ");
      Serial.println(bpmVal);

      if (SerialBT.available()) {
        char userBT[40];
        //sprintf(userBT, "u%.1fb%s", tmp006.readObjTempC(), bpmVal);
        SerialBT.write((uint8_t*)userBT, strlen(userBT));

      }
    }
  }
}

/*
   MQ-2 Init
*/

void mq2_init() {
  mq2.begin();
}


/*
   MQ-2 Read
*/
void mq2_read() {
  float* values = mq2.read(true); //set it false if you don't want to print the values in the Serial

  //Reading specific values:
  //lpg = values[0];
  lpg = mq2.readLPG();
  //co = values[1];
  co = mq2.readCO();
  //smoke = values[2];
  smoke = mq2.readSmoke();
}
/*
   Initialization
*/
void setup() {
  Serial.begin(115200);
  Serial1.begin(115200);

  while (! Serial) {
    delay(1);
  }

  Serial.println("Initialization");

  vl53l0x_init();
  vcnl4040_init();
  veml6070_init();
  neopixel_init();
  drv2605l_init();
  mq2_init();

  inputString.reserve(200);

  SerialBT.begin("ESP32_SmartHelmet");

  delay(200);
}


/*
   Main Loop
*/
void loop() {


  if (stringComplete) {
    Serial.println(inputString);
    // clear the string:
    inputString = "";
    stringComplete = false;

    mq2_read();

    inputString += 'l' + lpg + 'z' + co + 's' + smoke;

    char btString[128];
    inputString.toCharArray(btString, sizeof(btString));

    SerialBT.write((uint8_t *)btString, strlen(btString));
  }

  serialEvent();

  bt_serial_read();
}


/*
  SerialEvent occurs whenever a new data comes in the hardware serial RX. This
  routine is run between each time loop() runs, so using delay inside loop can
  delay response. Multiple bytes of data may be available.
*/
void serialEvent() {
  while (Serial1.available()) {
    char inChar = (char)Serial1.read();

    if (inChar == '\n') {
      stringComplete = true;
      break;
    }
    inputString += inChar;
  }
}

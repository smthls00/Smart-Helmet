/*
   Smart helmet code for HUZZAH32


   MCU: ESP32/Huzzah Board

   Sensors: VCNL4040, BME680, VL53L0X, MQ-2, DRV2605L, VEML6070, CCS811, Neopixel, Sharp IR


   Author: Anar Aliyev
*/


/*
   Includes
*/
#include <Wire.h>
#include <BluetoothSerial.h>

#include <MQ2Lib.h>
#include <SharpIR.h>

#include <Adafruit_Sensor.h>
#include <Adafruit_VL53L0X.h>
#include <Adafruit_VCNL4040.h>
#include <Adafruit_VEML6070.h>
#include <Adafruit_NeoPixel.h>
#include <Adafruit_DRV2605.h>
#include <Adafruit_CCS811.h>
#include <Adafruit_BME680.h>


/*
   Defines
*/
//#define DEBUG 1


#define FORWARD_DIRECTION 0
#define BACKWARD_DIRECTION 1
#define RIGHT_DIRECTION 2
#define LEFT_DIRECTION 3

#define NUM_PIXELS 8
#define UPPER_THRESHOLD 10000
#define CCS811_ADDR 0x5B

#define SEALEVELPRESSURE_HPA (1013.25)
#define SHARPIR_MODEL 1080

#define MQ2_PIN A2
#define SHARPIR_PIN A3
#define BATTERY_PIN A13
#define NEOPIXEL_PIN 21


/*
   Global sensors objects
*/
Adafruit_VL53L0X vl53l0xObj;
Adafruit_VCNL4040 vcnl4040Obj;
Adafruit_VEML6070 veml6070Obj;
Adafruit_CCS811 ccs811Obj;
Adafruit_BME680 bme680Obj;
Adafruit_DRV2605 drv2605lObj;
MQ2 mq2Obj(MQ2_PIN, false); //instance (true=with serial output enabled

SharpIR sharpIRObj(SHARPIR_PIN, SHARPIR_MODEL);

Adafruit_NeoPixel pixelsObj(NUM_PIXELS, NEOPIXEL_PIN, NEO_GRB + NEO_KHZ800);

BluetoothSerial SerialBTObj;


/*
   Global variables
*/
bool BTStopSend = false;
bool BTConnected = false;
char teensyDataString[16];
char esp32DataString[64];

bool teensySerialComplete = false;

uint16_t forwardThreshold = 500;
uint16_t backwardThreshold = 40;

uint16_t eco2Val;
uint16_t tvocVal;

float lpgVal;
float coVal;
float smkVal;

long batteryMillis;
long pixelsColorsMillis;

uint32_t pixelsColors[NUM_PIXELS] = {0};


/*
   VCNL4040 Init
*/
void vcnl4040Init() {
#ifdef DEBUG
  Serial.println(F("VCNL4040 Init"));
#endif

  if (!vcnl4040Obj.begin()) {
#ifdef DEBUG
    Serial.println(F("VCNL4040 Fail"));
#endif

    while (1);
  }

#ifdef DEBUG
  Serial.println(F("VCNL4040 Done"));
#endif
}


/*
   VCNL4040 Read
*/
void vcnl4040_read() {
#ifdef DEBUG
  Serial.println(F("VCNL4040"));

  Serial.print(F("Proximity:")); Serial.println(vcnl4040Obj.getProximity());
  Serial.print(F("Ambient light:")); Serial.println(vcnl4040Obj.getAmbientLight());
  Serial.print(F("White light:")); Serial.println(vcnl4040Obj.getWhiteLight());
#endif
}


/*
   VL53L0X init
*/
void vl53l0xInit() {
#ifdef DEBUG
  Serial.println(F("VL53L0X Init"));
#endif

  if (!vl53l0xObj.begin()) {
#ifdef DEBUG
    Serial.println(F("VL53L0X Fail"));
#endif
    while (1);
  }

#ifdef DEBUG
  Serial.println(F("VL53L0X Done"));
#endif
}


/*
   VL53L0X read
*/
void vl53l0xRead() {
  VL53L0X_RangingMeasurementData_t vl53l0xData;

  vl53l0xObj.rangingTest(&vl53l0xData, false); // pass in 'true' to get debug data printout!

  if (vl53l0xData.RangeStatus != 4 && vl53l0xData.RangeMilliMeter < 8191 && vl53l0xData.RangeMilliMeter > 0) {  // phase failures have incorrect data

#ifdef DEBUG
    Serial.print(F("Distance (mm): "));
    Serial.println(vl53l0xData.RangeMilliMeter);
#endif

    if (vl53l0xData.RangeMilliMeter >= 1000) {
      //dangerAlert(FORWARD_DIRECTION);
      //liteAlert(FORWARD_DIRECTION, false);
    }

    else if (vl53l0xData.RangeMilliMeter < 1000 && vl53l0xData.RangeMilliMeter > 300) {
      dangerAlert(FORWARD_DIRECTION);
      //liteAlert(FORWARD_DIRECTION, true);
    }

    else if (vl53l0xData.RangeMilliMeter <= 300) {
      dangerAlert(FORWARD_DIRECTION);
      //liteAlert(FORWARD_DIRECTION, true);
    }
  }
}


/*
   Sharp IR read
*/
void sharpIRRead() {
  int sharpIRDistanceVal = sharpIRObj.distance();

  if (sharpIRDistanceVal > 80)
    sharpIRDistanceVal = 80;

  if (sharpIRDistanceVal > 0) {

#ifdef DEBUG
    Serial.println(sharpIRDistanceVal); //Print the value to the serial monitor
#endif

    if (sharpIRDistanceVal >= 50) {
      //dangerAlert(BACKWARD_DIRECTION, false);
      //liteAlert(BACKWARD_DIRECTION);
    }

    else if (sharpIRDistanceVal < 50 && sharpIRDistanceVal > 30) {
      //dangerAlert(BACKWARD_DIRECTION, false);
      liteAlert(BACKWARD_DIRECTION);
    }

    else if (sharpIRDistanceVal <= 30) {
      //dangerAlert(BACKWARD_DIRECTION, true);
      liteAlert(BACKWARD_DIRECTION);
    }
  }
}


/*
   VEML6070 Init
*/
void veml6070Init() {
#ifdef DEBUG
  Serial.println(F("VEML6070 Init"));
#endif

  veml6070Obj.begin(VEML6070_1_T);

#ifdef DEBUG
  Serial.println(F("VEML6070 Done"));
#endif
}


/*
   VEML6070 Read
*/
void veml6070Read() {

  uint16_t ambientLightVal = veml6070Obj.readUV();

#ifdef DEBUG
  Serial.print(F("ambientLightVal "));
  Serial.println(ambientLightVal);
#endif

  if (ambientLightVal == 0)
    flashlightAlert();
  else
    clearPixelsColors();
}


/*
   CCS811 Init
*/
void ccs811Init() {

#ifdef DEBUG
  Serial.println(F("CCS811 Init"));
#endif

  if (!ccs811Obj.begin(CCS811_ADDR)) {
#ifdef DEBUG
    Serial.println(F("CCS811 Fail"));
#endif
    while (1);
  }

  // Wait for the sensor to be ready
  while (!ccs811Obj.available());

#ifdef DEBUG
  Serial.println(F("CCS811 Done"));
#endif
}


/*
   CCS811 Read
*/
void ccs811Read() {
  if (ccs811Obj.available()) {
    ccs811Obj.readData();

    eco2Val = ccs811Obj.geteCO2();
    tvocVal = ccs811Obj.getTVOC();

    if (eco2Val > UPPER_THRESHOLD)
      eco2Val = UPPER_THRESHOLD;

    if (tvocVal > UPPER_THRESHOLD)
      tvocVal = UPPER_THRESHOLD;
  }
}


/*
   BME680 Init
*/
void bme680Init() {
#ifdef DEBUG
  Serial.println(F("BME680 Init"));
#endif

  if (!bme680Obj.begin()) {
#ifdef DEBUG
    Serial.println(F("BME680 Fail"));
#endif
    while (1);
  }

  // Set up oversampling and filter initialization
  bme680Obj.setTemperatureOversampling(BME680_OS_8X);
  bme680Obj.setHumidityOversampling(BME680_OS_2X);
  bme680Obj.setPressureOversampling(BME680_OS_4X);
  bme680Obj.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme680Obj.setGasHeater(320, 150); // 320*C for 150 ms

#ifdef DEBUG
  Serial.println(F("BME680 Done"));
#endif
}


/*
   BME680 Read
*/
void bme680Read() {
  if (!bme680Obj.performReading()) {
#ifdef DEBUG
    Serial.println(F("BME680 Fail"));
#endif
    while (1);
  }
}


/*
   MQ-2 Init
*/
void mq2Init() {
  mq2Obj.begin();
}


/*
   MQ-2 Read
*/
void mq2Read() {
  lpgVal = mq2Obj.readLPG();
  coVal = mq2Obj.readCO();
  smkVal = mq2Obj.readSmoke();

  if (lpgVal > UPPER_THRESHOLD)
    lpgVal = UPPER_THRESHOLD;

  if (coVal > UPPER_THRESHOLD)
    coVal = UPPER_THRESHOLD;

  if (smkVal > UPPER_THRESHOLD)
    smkVal = UPPER_THRESHOLD;
}


/*
   DRV2605L Init
*/
void drv2605lInit() {
#ifdef DEBUG
  Serial.println(F("DRV2605L Init"));
#endif

  if (!drv2605lObj.begin()) {
#ifdef DEBUG
    Serial.println(F("DRV2605L Fail"));
#endif
    while (1);
  };

  drv2605lObj.selectLibrary(1);

  // I2C trigger by sending 'go' command
  // default, internal trigger when sending GO command
  drv2605lObj.setMode(DRV2605_MODE_INTTRIG);

  drv2605lObj.setWaveform(0, 14);  // play effect
  drv2605lObj.setWaveform(1, 0);   // end waveform

#ifdef DEBUG
  Serial.println(F("DRV2605L Done"));
#endif
}


/*
   DRV2605L Action
*/
void drv2605lAction() {
  drv2605lObj.go();
}


/*
   NEOPIXEL Init
*/
void neopixelsInit() {
  pixelsObj.begin();
}


/*
   Lite distance alert feedback for neopixel
*/
void liteAlert(int alertDirection) {
  if (alertDirection == BACKWARD_DIRECTION) {
    pixelsColors[0] = pixelsObj.Color(255, 0, 0);
    pixelsColors[NUM_PIXELS - 1] = pixelsObj.Color(255, 0, 0);
  }

  else if (alertDirection == FORWARD_DIRECTION) {
    pixelsColors[2] = pixelsObj.Color(255, 0, 0);
    pixelsColors[5] = pixelsObj.Color(255, 0, 0);
  }
}


/*
   Danger distance alert feedback for neopixel
*/
void dangerAlert(int alertDirection) {
  if (alertDirection == BACKWARD_DIRECTION) {
    pixelsColors[1] = pixelsObj.Color(255, 0, 0);
    pixelsColors[NUM_PIXELS - 2] = pixelsObj.Color(255, 0, 0);
  }

  else if (alertDirection == FORWARD_DIRECTION) {
    pixelsColors[3] = pixelsObj.Color(255, 0, 0);
    pixelsColors[4] = pixelsObj.Color(255, 0, 0);
  }
}


/*
   Slide alert from smartphone to neopixel
*/
void slideAlert(int slideDirection) {
  pixelsObj.clear();

  if (slideDirection == RIGHT_DIRECTION) {
    for (int i = 0; i < NUM_PIXELS; i++) {
      pixelsObj.setPixelColor(i, pixelsObj.Color(0, 200, 0));
      pixelsObj.show();
      delay(250);
      pixelsObj.setPixelColor(i, pixelsObj.Color(0, 0, 0));
    }
  }

  else if (slideDirection == LEFT_DIRECTION) {
    for (int i = NUM_PIXELS - 1; i >= 0; i--) {
      pixelsObj.setPixelColor(i, pixelsObj.Color(0, 200, 0));
      pixelsObj.show();
      delay(250);
      pixelsObj.setPixelColor(i, pixelsObj.Color(0, 0, 0));
    }
  }

  delay(1000);
}


/*
   Flashlight alert feedback from VEML6070 to neopixel
*/
void flashlightAlert() {
  for (int i = 0; i < NUM_PIXELS; i++) {
    pixelsColors[i] = pixelsObj.Color(255, 255, 255);
  }
}


/*
   Clear pixel colors array
*/
void clearPixelsColors() {
  for (int i = 0; i < NUM_PIXELS; i++) {
    pixelsColors[i] = 0;
  }
}


/*
   Update pixels array
*/
void updatePixelsColors() {
  for (int i = 0; i < NUM_PIXELS; i++) {
    if (pixelsColors[i] != pixelsObj.getPixelColor(i)) {
      pixelsObj.setPixelColor(i, pixelsColors[i]);
      pixelsObj.show();
    }
  }
}


/*
   Threshold BT Alert
*/
void BTThresholdAlert() {
  for (int i = 0; i < NUM_PIXELS; i++) {
    pixelsObj.setPixelColor(i, pixelsObj.Color(255, 165, 0));
    pixelsObj.show();
  }

  delay(2000);
}


/*
   Read Incoming BT Data
*/
void BTInputRead() {
  if (SerialBTObj.available()) {
    char BTCommand[16];
    byte k = 0;
    while (SerialBTObj.available()) {
      char inChar = (char)SerialBTObj.read();

      if (inChar == '\n') {
        break;
      }
      BTCommand[k++] = inChar;
    }

#ifdef DEBUG
    Serial.print(F("BTCommand:"));
    Serial.println(BTCommand);
#endif

    switch (BTCommand[0]) {
      case 'l':
        drv2605lAction();
        slideAlert(LEFT_DIRECTION);
        break;
      case 'r':
        drv2605lAction();
        slideAlert(RIGHT_DIRECTION);
        break;
      case 'v':
        drv2605lAction();
        BTThresholdAlert();
        break;
      case 's':
        BTStopSend = true;
        break;
      case 'c':
        BTStopSend = false;
        break;
      case 'f':
        forwardThreshold = atoi(&BTCommand[1]);
        if (forwardThreshold < 0)
          forwardThreshold = 10;
        else if (forwardThreshold > 1500)
          forwardThreshold = 1500;
        break;
      default:
#ifdef DEBUG
        Serial.println(F("non-recognizable command"));
#endif
        break;
    }
  }
}


/*
   Read battery voltage
*/
void batteryMonitor() {
  if (millis() - batteryMillis >= 10000) {

    batteryMillis = millis();

    float batteryVoltage = analogRead(BATTERY_PIN);

    sprintf(esp32DataString, "r%.1f", batteryVoltage);


    if (BTConnected) {
      SerialBTObj.write((uint8_t *)esp32DataString, strlen(esp32DataString));
    }

#ifdef DEBUG
    Serial.print(F("Battery Voltage:"));
    Serial.println(batteryVoltage);
#endif
  }
}


/*
   Send ESP32 sensors data to BT
*/
void esp32BTSend() {
  sprintf(esp32DataString, "c%dv%do%.1fp%.1fh%.1fg%.1fa%.1fl%.1fz%.1fs%.1f", eco2Val, tvocVal,
          bme680Obj.temperature, bme680Obj.pressure / 100.0, bme680Obj.humidity,
          bme680Obj.gas_resistance / 1000.0, bme680Obj.readAltitude(SEALEVELPRESSURE_HPA),
          lpgVal, coVal, smkVal);

  if (BTConnected && !BTStopSend) {
    SerialBTObj.write((uint8_t *)esp32DataString, strlen(esp32DataString));
  }

#ifdef DEBUG
  Serial.println(esp32DataString);
#endif
}


/*
   Send Teensy sensors data to BT
*/
void teensyBTSend() {
  if (teensySerialComplete) {
    if (BTConnected && !BTStopSend)
    {
      SerialBTObj.write((uint8_t *)teensyDataString, strlen(teensyDataString));
    }

#ifdef DEBUG
    Serial.println(teensyDataString);
#endif

    memset(teensyDataString, 0, sizeof(char));
    teensySerialComplete = false;
  }
}


/*
   Read incoming data from Teensy
*/
void teensySerialEvent() {
  byte k = 0;
  while (Serial1.available()) {
    char inChar = (char)Serial1.read();

    if (inChar == '\n') {
      teensySerialComplete = true;
      break;
    }
    teensyDataString[k++] = inChar;
  }
}


/*
   Connected/Disconnected callback
*/
void BTCallback(esp_spp_cb_event_t event, esp_spp_cb_param_t *param) {
  if (event == ESP_SPP_SRV_OPEN_EVT) {
    BTConnected = true;
  }
  else if (event == ESP_SPP_CLOSE_EVT) {
    BTConnected = false;
  }
}


/*
   Calibration
*/
void calibration() {
  for (int i = 0; i < NUM_PIXELS; i++) {
    sensorsRead();
    pixelsObj.setPixelColor(i, pixelsObj.Color(0, 255, 0));
    pixelsObj.show();
  }

  pixelsObj.clear();
  pixelsObj.show();
}


/*
   Init all sensors
*/
void sensorsInit() {
  vl53l0xInit();
  vcnl4040Init();
  veml6070Init();
  drv2605lInit();
  bme680Init();
  ccs811Init();
  mq2Init();
  neopixelsInit();
}


/*
   Read all sensors
*/
void sensorsRead() {
  veml6070Read();
  vl53l0xRead();
  sharpIRRead();
  mq2Read();
  ccs811Read();
  bme680Read();
  //updatePixelsColors();
}


/*
   Initialization
*/
void setup() {
#ifdef DEBUG
  Serial.begin(9600);
#endif

  Serial1.begin(9600);

#ifdef DEBUG
  Serial.println(F("Initialization"));
#endif

  analogReadResolution(10);

  sensorsInit();

  calibration();

  SerialBTObj.begin("ESP32_SmartHelmet");

  SerialBTObj.register_callback(BTCallback);
}


/*
   Main Loop
*/
void loop() {
  sensorsRead();

  esp32BTSend();

  teensySerialEvent();
  teensyBTSend();

  batteryMonitor();

  BTInputRead();
}

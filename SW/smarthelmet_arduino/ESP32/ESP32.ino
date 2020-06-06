/*

   Smart helmet code for HUZZAH32



   MCU: ESP32/Huzzah Board

   Sensors: VCNL4040, BME680, VL53L0X, MQ-2, DRV2605L, VEML6070, CCS811, Neopixel


   Author: Anar Aliyev




*/


/*
   Includes
*/
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_VL53L0X.h>
#include <Adafruit_VCNL4040.h>
#include <Adafruit_VEML6070.h>
#include <Adafruit_NeoPixel.h>
#include <Adafruit_DRV2605.h>
#include <Adafruit_CCS811.h>
#include <Adafruit_BME680.h>
#include <SharpIR.h>

#include <BluetoothSerial.h>
#include <MQ2Lib.h>


/*
   Defines
*/
#define FORWARD_DIRECTION 0
#define BACKWARD_DIRECTION 1
#define RIGHT_DIRECTION 2
#define LEFT_DIRECTION 3

#define DEBUG 1
//#define NEOPIXEL_CLEAR 0
//#define NEOPIXEL_RED 1
//#define NEOPIXEL_BLUE 2
//#define NEOPIXEL_GREEN 3
//#define NEOPIXEL_VIOLET 4

//#define NEOPIXEL_RUNNING 0
//#define NEOPIXEL_LIGHT 1
//#define NEOPIXEL_FORWARD 2
//#define NEOPIXEL_BACKWARD 3
//#define NEOPIXEL_BT 4
//#define NEOPIXEL_ERROR 5


#define UPPER_THRESHOLD 10000

#define CCS811_ADDR 0x5B
//#define LONG_RANGE

#define NEOPIXEL_PIN        21
#define NUM_PIXELS 8

#define MQ2_PIN A2

#define SEALEVELPRESSURE_HPA (1013.25)

#define SHARPIR_PIN A3
#define SHARPIR_MODEL 1080
/*
   Global sensors objects
*/

SharpIR SharpIR(SHARPIR_PIN, SHARPIR_MODEL);

Adafruit_VL53L0X lox;
Adafruit_VCNL4040 vcnl4040;
Adafruit_VEML6070 uv;
Adafruit_CCS811 ccs;
Adafruit_BME680 bme;
Adafruit_DRV2605 drv;
MQ2 mq2(MQ2_PIN, false); //instance (true=with serial output enabled

Adafruit_NeoPixel pixels(NUM_PIXELS, NEOPIXEL_PIN, NEO_GRB + NEO_KHZ800);

BluetoothSerial SerialBT;

bool stopBTCommand = false;
bool serialBT = false;
char inputString[16];         // a String to hold incoming data
char mainString[64];
bool stringComplete = false;  // whether the string is complete
float lpg = 0, co = 0, smoke = 0;

uint8_t forwardDistance = 500;
uint8_t backwardDistance = 40;

uint16_t eco2;
uint16_t tvoc;

long batteryMillis;

/*
   VL53L0X Init
*/
void vl53l0x_init() {
#ifdef DEBUG
  Serial.println(F("VL53L0X Init"));
#endif

  if (!lox.begin()) {
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
   VL53L0X Read
*/
void vl53l0x_read() {
  VL53L0X_RangingMeasurementData_t measure;

  lox.rangingTest(&measure, false); // pass in 'true' to get debug data printout!

  if (measure.RangeStatus != 4 && measure.RangeMilliMeter < 8191) {  // phase failures have incorrect data

#ifdef DEBUG
    Serial.print(F("Distance (mm): "));
    Serial.println(measure.RangeMilliMeter);
#endif

    if (measure.RangeMilliMeter >= 1000) {
      DangerAlert(FORWARD_DIRECTION, false);
      LiteAlert(FORWARD_DIRECTION, false);
    }

    else if (measure.RangeMilliMeter < 1000 && measure.RangeMilliMeter > 300 ) {
      DangerAlert(FORWARD_DIRECTION, false);
      LiteAlert(FORWARD_DIRECTION, true);
    }

    else if (measure.RangeMilliMeter <= 300) {
      DangerAlert(FORWARD_DIRECTION, true);
      LiteAlert(FORWARD_DIRECTION, true);
    }
  }


  int sharpIRDistance = SharpIR.distance();

  if (sharpIRDistance <= 80 && sharpIRDistance > 0) {

#ifdef DEBUG
    Serial.println(sharpIRDistance); //Print the value to the serial monitor
#endif

    if (sharpIRDistance >= 70) {
      DangerAlert(BACKWARD_DIRECTION, false);
      LiteAlert(BACKWARD_DIRECTION, false);
    }

    else if (sharpIRDistance < 70 && sharpIRDistance > 30) {
      DangerAlert(BACKWARD_DIRECTION, false);
      LiteAlert(BACKWARD_DIRECTION, true);
    }

    else if (sharpIRDistance <= 30) {
      DangerAlert(BACKWARD_DIRECTION, true);
      LiteAlert(BACKWARD_DIRECTION, true);
    }
  }
}


void LiteAlert(int alertDirection, bool setFlag) {

  if (alertDirection == BACKWARD_DIRECTION) {

//    for(int i = 0; i < NUM_PIXELS; i ++){
//
//      if(i == 0)
//        pixels.setPixelColor(i, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
//
//      else if(i == NUM_PIXELS - 1)
//        pixels.setPixelColor(i, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
//
//      else
//        pixels.setPixelColor(i, pixels.getPixelColor(i));
//    }
    pixels.setPixelColor(0, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
    pixels.setPixelColor(NUM_PIXELS - 1, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
  }

  else if (alertDirection == FORWARD_DIRECTION) {
//    for(int i = 0; i < NUM_PIXELS; i ++){
//
//      if(i == 2)
//        pixels.setPixelColor(i, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
//
//      else if(i == 5)
//        pixels.setPixelColor(i, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
//
//      else
//        pixels.setPixelColor(i, pixels.getPixelColor(i));
//    }
    pixels.setPixelColor(2, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
    pixels.setPixelColor(5, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
  }

  pixels.show();
  //delay(1);
}


void DangerAlert(int alertDirection, bool setFlag) {

  if (alertDirection == BACKWARD_DIRECTION) {
//    for(int i = 0; i < NUM_PIXELS; i ++){
//
//      if(i == 1)
//        pixels.setPixelColor(i, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
//
//      else if(i == NUM_PIXELS - 2)
//        pixels.setPixelColor(i, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
//
//      else
//        pixels.setPixelColor(i, pixels.getPixelColor(i));
//    }
    pixels.setPixelColor(1, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
    pixels.setPixelColor(NUM_PIXELS - 2, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
  }

  else if (alertDirection == FORWARD_DIRECTION) {
//    for(int i = 0; i < NUM_PIXELS; i ++){
//
//      if(i == 3)
//        pixels.setPixelColor(i, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
//
//      else if(i == 4)
//        pixels.setPixelColor(i, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
//
//      else
//        pixels.setPixelColor(i, pixels.getPixelColor(i));
//    }
    pixels.setPixelColor(3, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
    pixels.setPixelColor(4, setFlag ? pixels.Color(200, 0, 0) : pixels.Color(0, 0, 0));
  }

  pixels.show();
  //delay(1);
}



void slideAlert(int slideDirection) {
  pixels.clear();
  pixels.show();

  if (slideDirection == RIGHT_DIRECTION) {
    for (int i = 0; i < NUM_PIXELS; i++) {
      pixels.setPixelColor(i, pixels.Color(0, 200, 0));
      pixels.show();
      delay(500);
    }
  }

  else if (slideDirection == LEFT_DIRECTION) {
    for (int i = NUM_PIXELS - 1; i >= 0; i--) {
      pixels.setPixelColor(i, pixels.Color(0, 200, 0));
      pixels.show();
      delay(500);
    }
  }

  delay(1000);
}


void flashlightAlert() {
  //  pixels.clear();
  //  pixels.show();
  //
  //  for (int i = 0; i < NUM_PIXELS; i++) {
  //    pixels.setPixelColor(i, pixels.Color(0, 0, 100));
  //
  //
  //    pixels.show();
  //  }
  //
  //  delay(1000);
  //
  //  pixels.clear();
  //  pixels.show();
}


/*
   VCNL4040 Init
*/
void vcnl4040_init() {
#ifdef DEBUG
  Serial.println(F("VCNL4040 Init"));
#endif

  if (!vcnl4040.begin()) {
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

  Serial.print(F("Proximity:")); Serial.println(vcnl4040.getProximity());
  Serial.print(F("Ambient light:")); Serial.println(vcnl4040.getAmbientLight());
  Serial.print(F("White light:")); Serial.println(vcnl4040.getWhiteLight());
#endif
}


/*
   VEML6070 Init
*/
void veml6070_init() {
#ifdef DEBUG
  Serial.println(F("VEML6070 Init"));
#endif

  uv.begin(VEML6070_1_T);

#ifdef DEBUG
  Serial.println(F("VEML6070 Done"));
#endif
}


/*
   VEML6070 Read
*/
void veml6070_read() {

  uint16_t ambientLight = uv.readUV();

#ifdef DEBUG
  Serial.print(F("ambientLight "));
  Serial.println(ambientLight);
#endif

  if (ambientLight == 0)
    flashlightAlert();
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
   Read Incoming BT Data
*/
void bt_serial_read() {
  if (SerialBT.available()) {
    char btCommand[16];
    byte k = 0;
    while (SerialBT.available()) {
      char inChar = (char)SerialBT.read();

      if (inChar == '\n') {
        break;
      }
      btCommand[k++] = inChar;
    }

#ifdef DEBUG
    Serial.print(F("BTCommand:"));
    Serial.println(btCommand);
#endif

    switch (btCommand[0]) {
      case 'l':
        drv2605l_set();
        slideAlert(LEFT_DIRECTION);
        break;
      case 'r':
        slideAlert(RIGHT_DIRECTION);
        drv2605l_set();
        break;
      case 'v':
        drv2605l_set();
        break;
      case 's':
        stopBTCommand = true;
        break;
      case 'c':
        stopBTCommand = false;
        break;
      case 'f':
        forwardDistance = atoi(&btCommand[1]);
        if (forwardDistance < 0)
          forwardDistance = 10;
        else if (forwardDistance > 1500)
          forwardDistance = 1500;
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
   DRV2605L Init
*/
void drv2605l_init() {
#ifdef DEBUG
  Serial.println(F("DRV2605L Init"));
#endif

  if (!drv.begin()) {
#ifdef DEBUG
    Serial.println(F("DRV2605L Fail"));
#endif
    while (1);
  };

  drv.selectLibrary(1);

  // I2C trigger by sending 'go' command
  // default, internal trigger when sending GO command
  drv.setMode(DRV2605_MODE_INTTRIG);

#ifdef DEBUG
  Serial.println(F("DRV2605L Done"));
#endif
}


/*
   DRV2605L Set
*/
void drv2605l_set() {
  // set the effect to play
  drv.setWaveform(0, 14);  // play effect
  drv.setWaveform(1, 0);   // end waveform

  // play the effect!
  drv.go();
}

/*
   CCS811 Init
*/
void ccs811_init() {

#ifdef DEBUG
  Serial.println(F("CCS811 Init"));
#endif

  if (!ccs.begin(CCS811_ADDR)) {
#ifdef DEBUG
    Serial.println(F("CCS811 Fail"));
#endif
    while (1);
  }

  // Wait for the sensor to be ready
  while (!ccs.available());

#ifdef DEBUG
  Serial.println(F("CCS811 Done"));
#endif
}


/*
   CCS811 Read
*/
void ccs811_read() {

  if (ccs.available()) {
    ccs.readData();

    eco2 = ccs.geteCO2();
    tvoc = ccs.getTVOC();

    if (eco2 > UPPER_THRESHOLD)
      eco2 = UPPER_THRESHOLD;

    if (tvoc > UPPER_THRESHOLD)
      tvoc = UPPER_THRESHOLD;
  }

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
   BME680 Init
*/
void bme680_init() {
#ifdef DEBUG
  Serial.println(F("BME680 Init"));
#endif

  if (!bme.begin()) {
#ifdef DEBUG
    Serial.println(F("BME680 Fail"));
#endif
    while (1);
  }

  // Set up oversampling and filter initialization
  bme.setTemperatureOversampling(BME680_OS_8X);
  bme.setHumidityOversampling(BME680_OS_2X);
  bme.setPressureOversampling(BME680_OS_4X);
  bme.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme.setGasHeater(320, 150); // 320*C for 150 ms

#ifdef DEBUG
  Serial.println(F("BME680 Done"));
#endif
}


/*
   BME680 Read
*/
void bme680_read() {
  //Serial.println("BME680");

  if (!bme.performReading()) {
#ifdef DEBUG
    Serial.println(F("BME680 Fail"));
#endif
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
   MQ-2 Init
*/

void mq2_init() {
  mq2.begin();
}


/*
   MQ-2 Read
*/
void mq2_read() {
  float* values = mq2.read(false); //set it false if you don't want to print the values in the Serial

  //Reading specific values:
  //lpg = values[0];
  lpg = mq2.readLPG();
  //co = values[1];
  co = mq2.readCO();
  //smoke = values[2];
  smoke = mq2.readSmoke();


  if (lpg > UPPER_THRESHOLD)
    lpg = UPPER_THRESHOLD;

  if (co > UPPER_THRESHOLD)
    co = UPPER_THRESHOLD;

  if (smoke > UPPER_THRESHOLD)
    smoke = UPPER_THRESHOLD;

  //  Serial.print("lpg ");
  //  Serial.println(lpg);
  //  Serial.print("co ");
  //  Serial.println(co);
  //  Serial.print("smoke ");
  //  Serial.println(smoke);
}

void calibration() {
  for (int i = 0; i < 10; i++) {
    veml6070_read();
    vl53l0x_read();
    mq2_read();
    ccs811_read();
    bme680_read();
  }
}

void batteryMonitor() {

  if (millis() - batteryMillis >= 5000) {

    batteryMillis = millis();

    float batteryVoltage = analogRead(A13);

    sprintf(mainString, "r%.1f", batteryVoltage);


    if (serialBT) {
      SerialBT.write((uint8_t *)mainString, strlen(mainString));
    }

#ifdef DEBUG
    Serial.print(F("Battery Voltage:"));
    Serial.println(batteryVoltage);
#endif
  }
}


void send2BT() {
  sprintf(mainString, "c%dv%do%.1fp%.1fh%.1fg%.1fa%.1fl%.1fz%.1fs%.1f", eco2, tvoc,
          bme.temperature, bme.pressure / 100.0, bme.humidity,
          bme.gas_resistance / 1000.0, bme.readAltitude(SEALEVELPRESSURE_HPA),
          lpg, co, smoke);

  if (serialBT && !stopBTCommand) {
    SerialBT.write((uint8_t *)mainString, strlen(mainString));
  }

#ifdef DEBUG
  Serial.println(mainString);
#endif
}

void teensySend2BT() {
  if (stringComplete) {
    //Serial.println(inputString);
    if (serialBT && !stopBTCommand)
    {
      //Serial.println("AvailableBT");
      SerialBT.write((uint8_t *)inputString, strlen(inputString));
    }

    //Serial.println("inputString");
#ifdef DEBUG
    Serial.println(inputString);
#endif

    memset(inputString, 0, sizeof(char));
    stringComplete = false;
  }
}
/*
   Initialization
*/
void setup() {
#ifdef DEBUG
  Serial.begin(9600);
#endif

  Serial1.begin(9600);

  while (! Serial) {
    delay(1);
  }

#ifdef DEBUG
  Serial.println(F("Initialization"));
#endif

  analogReadResolution(10);

  neopixel_init();
  vl53l0x_init();
  vcnl4040_init();
  veml6070_init();
  drv2605l_init();
  bme680_init();
  ccs811_init();
  mq2_init();

  SerialBT.begin("ESP32_SmartHelmet");

  SerialBT.register_callback(BTcallback);

  calibration();
}


/*
   Main Loop
*/
void loop() {
  veml6070_read();
  vl53l0x_read();
  mq2_read();
  ccs811_read();
  bme680_read();

  send2BT();

  serialEvent();
  teensySend2BT();

  bt_serial_read();


  batteryMonitor();
}


/*
  SerialEvent occurs whenever a new data comes in the hardware serial RX. This
  routine is run between each time loop() runs, so using delay inside loop can
  delay response. Multiple bytes of data may be available.
*/
void serialEvent() {
  byte k = 0;
  while (Serial1.available()) {
    char inChar = (char)Serial1.read();

    if (inChar == '\n') {
      stringComplete = true;
      break;
    }
    inputString[k++] = inChar;
  }
}


void BTcallback(esp_spp_cb_event_t event, esp_spp_cb_param_t *param) {
  if (event == ESP_SPP_SRV_OPEN_EVT) {
    serialBT = true;
  }
  else if (event == ESP_SPP_CLOSE_EVT) {
    serialBT = false;
  }
}

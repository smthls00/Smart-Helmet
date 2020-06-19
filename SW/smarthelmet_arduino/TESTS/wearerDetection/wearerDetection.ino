#include <Adafruit_VCNL4040.h>
#include <Adafruit_NeoPixel.h>

#define PIN 21
#define NUMPIXELS 8

#define RED pixels.Color(255, 0, 0)
#define GREEN pixels.Color(0, 255, 0)
#define BLUE pixels.Color(0, 0, 255)


Adafruit_VCNL4040 vcnl4040 = Adafruit_VCNL4040();
Adafruit_NeoPixel pixels(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);

char teensyDataString[16];
bool teensySerialComplete = false;

//Static Patterns
struct {
  int len = 1;
  uint32_t Colors[1][NUMPIXELS] = {{RED, 0, 0, 0, 0, 0, 0, 0}};
} farLeftPatternStatic;

struct {
  int len = 1;
  uint32_t Colors[1][NUMPIXELS] = {{RED, RED, 0, 0, 0, 0, 0, 0}};
} nearLeftPatternStatic;

struct {
  int len = 1;
  uint32_t Colors[1][NUMPIXELS] = {{RED, 0, 0, 0, 0, 0, 0, 0}};
} farLeftPatternStatic;

struct {
  int len = 1;
  uint32_t Colors[1][NUMPIXELS] = {{RED, RED, 0, 0, 0, 0, 0, 0}};
} nearLeftPatternStatic;

struct {
  int len = 1;
  uint32_t Colors[1][NUMPIXELS] = {{0, 0, 0, RED, RED, 0, 0, 0}};
} farHeadUpPatternStatic;


struct {
  int len = 1;
  uint32_t Colors[1][NUMPIXELS] = {{RED, 0, 0, 0, 0, 0, 0, RED}};
} backPatternStatic;



//Dynamic Patterns
struct {
  int len = 4;
  int pace = 150;
  uint32_t Colors[4][NUMPIXELS] = {
    {0, 0, 0, RED, 0, 0, 0, 0},
    {0, 0, RED, 0, 0, 0, 0, 0},
    {0, RED, 0, 0, 0, 0, 0, 0},
    {RED, 0, 0, 0, 0, 0, 0, 0}
  };
} goLeftPatternDynamic;

struct {
  int len = 4;
  int pace = 150;
  uint32_t Colors[4][NUMPIXELS] = {
    {0, 0, 0, 0, RED, 0, 0, 0},
    {0, 0, 0, 0, 0, RED, 0, 0},
    {0, 0, 0, 0, 0, 0, RED, 0},
    {0, 0, 0, 0, 0, 0, 0, RED}
  };
} goRightPatternDynamic;



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
   Send Teensy sensors data to BT
*/
void teensyPrint() {
  if (teensySerialComplete) {
    Serial.println(teensyDataString);

    memset(teensyDataString, 0, sizeof(char));
    teensySerialComplete = false;
  }
}

void vcnl4040Init() {
  if (!vcnl4040.begin()) {
    Serial.println("Couldn't find VCNL4040 chip");
    while (1);
  }
  Serial.println("Found VCNL4040 chip");

  //vcnl4040.setProximityLEDCurrent(VCNL4040_LED_CURRENT_200MA);
  Serial.print("Proximity LED current set to: ");
  switch (vcnl4040.getProximityLEDCurrent()) {
    case VCNL4040_LED_CURRENT_50MA: Serial.println("50 mA"); break;
    case VCNL4040_LED_CURRENT_75MA: Serial.println("75 mA"); break;
    case VCNL4040_LED_CURRENT_100MA: Serial.println("100 mA"); break;
    case VCNL4040_LED_CURRENT_120MA: Serial.println("120 mA"); break;
    case VCNL4040_LED_CURRENT_140MA: Serial.println("140 mA"); break;
    case VCNL4040_LED_CURRENT_160MA: Serial.println("160 mA"); break;
    case VCNL4040_LED_CURRENT_180MA: Serial.println("180 mA"); break;
    case VCNL4040_LED_CURRENT_200MA: Serial.println("200 mA"); break;
  }

  //vcnl4040.setProximityLEDDutyCycle(VCNL4040_LED_DUTY_1_40);
  Serial.print("Proximity LED duty cycle set to: ");
  switch (vcnl4040.getProximityLEDDutyCycle()) {
    case VCNL4040_LED_DUTY_1_40: Serial.println("1/40"); break;
    case VCNL4040_LED_DUTY_1_80: Serial.println("1/80"); break;
    case VCNL4040_LED_DUTY_1_160: Serial.println("1/160"); break;
    case VCNL4040_LED_DUTY_1_320: Serial.println("1/320"); break;
  }

  //vcnl4040.setAmbientIntegrationTime(VCNL4040_AMBIENT_INTEGRATION_TIME_80MS);
  Serial.print("Ambient light integration time set to: ");
  switch (vcnl4040.getAmbientIntegrationTime()) {
    case VCNL4040_AMBIENT_INTEGRATION_TIME_80MS: Serial.println("80 ms"); break;
    case VCNL4040_AMBIENT_INTEGRATION_TIME_160MS: Serial.println("160 ms"); break;
    case VCNL4040_AMBIENT_INTEGRATION_TIME_320MS: Serial.println("320 ms"); break;
    case VCNL4040_AMBIENT_INTEGRATION_TIME_640MS: Serial.println("640 ms"); break;
  }


  //vcnl4040.setProximityIntegrationTime(VCNL4040_PROXIMITY_INTEGRATION_TIME_8T);
  Serial.print("Proximity integration time set to: ");
  switch (vcnl4040.getProximityIntegrationTime()) {
    case VCNL4040_PROXIMITY_INTEGRATION_TIME_1T: Serial.println("1T"); break;
    case VCNL4040_PROXIMITY_INTEGRATION_TIME_1_5T: Serial.println("1.5T"); break;
    case VCNL4040_PROXIMITY_INTEGRATION_TIME_2T: Serial.println("2T"); break;
    case VCNL4040_PROXIMITY_INTEGRATION_TIME_2_5T: Serial.println("2.5T"); break;
    case VCNL4040_PROXIMITY_INTEGRATION_TIME_3T: Serial.println("3T"); break;
    case VCNL4040_PROXIMITY_INTEGRATION_TIME_3_5T: Serial.println("3.5T"); break;
    case VCNL4040_PROXIMITY_INTEGRATION_TIME_4T: Serial.println("4T"); break;
    case VCNL4040_PROXIMITY_INTEGRATION_TIME_8T: Serial.println("8T"); break;
  }

  //vcnl4040.setProximityHighResolution(false);
  Serial.print("Proximity measurement high resolution? ");
  Serial.println(vcnl4040.getProximityHighResolution() ? "True" : "False");
}

void setup() {
  Serial.begin(115200);
  Serial1.begin(9600);
  // Wait until serial port is opened
  while (!Serial) {
    delay(1);
  }

  vcnl4040Init();
  pixels.begin();
}

void loop() {
  int prox = vcnl4040.getProximity();
  Serial.print("Proximity:"); Serial.println(prox);

  if (prox >= 4) {
    for (int i = 0; i < NUMPIXELS; i++) {
      pixels.setPixelColor(i, pixels.Color(0, 150, 0));
    }
  } else
    pixels.clear();

  pixels.show();
  delay(500);


  teensySerialEvent();
  teensyPrint();
}

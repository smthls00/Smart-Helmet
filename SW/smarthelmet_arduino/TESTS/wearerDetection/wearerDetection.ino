#include <Adafruit_VCNL4040.h>
#include <Adafruit_NeoPixel.h>

#define PIN 21
#define NUMPIXELS 8

#define RED pixels.Color(255, 0, 0)
#define GREEN pixels.Color(0, 255, 0)
#define BLUE pixels.Color(0, 0, 255)
#define ORANGE pixels.Color(255, 165, 0)

#define SLEN 1
#define MLEN 5
#define DLEN 12

#define PACE 250


Adafruit_VCNL4040 vcnl4040 = Adafruit_VCNL4040();
Adafruit_NeoPixel pixels(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);

char teensyDataString[16];
bool teensySerialComplete = false;

bool flashlightFlag = false;

//Static Patterns

//A
uint32_t farLeftPatternStatic[SLEN][NUMPIXELS] = {{RED, 0, 0, 0, 0, 0, 0, 0}};

//B
uint32_t nearLeftPatternStatic[SLEN][NUMPIXELS] = {{RED, RED, 0, 0, 0, 0, 0, 0}};

//C
uint32_t farRightPatternStatic[SLEN][NUMPIXELS] = {{0, 0, 0, 0, 0, 0, 0, RED}};


//D
uint32_t nearRightPatternStatic[SLEN][NUMPIXELS] = {{0, 0, 0, 0, 0, 0, RED, RED}};


//E
uint32_t farHeadUpPatternStatic[SLEN][NUMPIXELS] = {{0, 0, 0, RED, RED, 0, 0, 0}};


//F
uint32_t nearHeadUpPatternStatic[SLEN][NUMPIXELS] = {{0, 0, RED, RED, RED, RED, 0, 0}};


//G
uint32_t farBackPatternStatic[SLEN][NUMPIXELS] = {{RED, 0, 0, 0, 0, 0, 0, RED}};


//H
uint32_t nearBackPatternStatic[SLEN][NUMPIXELS] = {{RED, RED, 0, 0, 0, 0, RED, RED}};



//Dynamic Patterns

//K
uint32_t goLeftPatternDynamic[DLEN][NUMPIXELS] = {
  {0, 0, 0, GREEN, 0, 0, 0, 0},
  {0, 0, GREEN, 0, 0, 0, 0, 0},
  {0, GREEN, 0, 0, 0, 0, 0, 0},
  {GREEN, 0, 0, 0, 0, 0, 0, 0},

  {0, 0, 0, GREEN, 0, 0, 0, 0},
  {0, 0, GREEN, 0, 0, 0, 0, 0},
  {0, GREEN, 0, 0, 0, 0, 0, 0},
  {GREEN, 0, 0, 0, 0, 0, 0, 0},

  {0, 0, 0, GREEN, 0, 0, 0, 0},
  {0, 0, GREEN, 0, 0, 0, 0, 0},
  {0, GREEN, 0, 0, 0, 0, 0, 0},
  {GREEN, 0, 0, 0, 0, 0, 0, 0}
};


//L
uint32_t goRightPatternDynamic[DLEN][NUMPIXELS] = {
  {0, 0, 0, 0, GREEN, 0, 0, 0},
  {0, 0, 0, 0, 0, GREEN, 0, 0},
  {0, 0, 0, 0, 0, 0, GREEN, 0},
  {0, 0, 0, 0, 0, 0, 0, GREEN},

  {0, 0, 0, 0, GREEN, 0, 0, 0},
  {0, 0, 0, 0, 0, GREEN, 0, 0},
  {0, 0, 0, 0, 0, 0, GREEN, 0},
  {0, 0, 0, 0, 0, 0, 0, GREEN},

  {0, 0, 0, 0, GREEN, 0, 0, 0},
  {0, 0, 0, 0, 0, GREEN, 0, 0},
  {0, 0, 0, 0, 0, 0, GREEN, 0},
  {0, 0, 0, 0, 0, 0, 0, GREEN}
};


//M
uint32_t goAheadPatternDynamic[DLEN][NUMPIXELS] = {
  {GREEN, 0, 0, 0, 0, 0, 0, GREEN},
  {0, GREEN, 0, 0, 0, 0, GREEN, 0},
  {0, 0, GREEN, 0, 0, GREEN, 0, 0},
  {0, 0, 0, GREEN, GREEN, 0, 0, 0},

  {GREEN, 0, 0, 0, 0, 0, 0, GREEN},
  {0, GREEN, 0, 0, 0, 0, GREEN, 0},
  {0, 0, GREEN, 0, 0, GREEN, 0, 0},
  {0, 0, 0, GREEN, GREEN, 0, 0, 0},

  {GREEN, 0, 0, 0, 0, 0, 0, GREEN},
  {0, GREEN, 0, 0, 0, 0, GREEN, 0},
  {0, 0, GREEN, 0, 0, GREEN, 0, 0},
  {0, 0, 0, GREEN, GREEN, 0, 0, 0}
};


//N
uint32_t goBackPatternDynamic[DLEN][NUMPIXELS] = {
  {0, 0, 0, GREEN, GREEN, 0, 0, 0},
  {0, 0, GREEN, 0, 0, GREEN, 0, 0},
  {0, GREEN, 0, 0, 0, 0, GREEN, 0},
  {GREEN, 0, 0, 0, 0, 0, 0, GREEN},

  {0, 0, 0, GREEN, GREEN, 0, 0, 0},
  {0, 0, GREEN, 0, 0, GREEN, 0, 0},
  {0, GREEN, 0, 0, 0, 0, GREEN, 0},
  {GREEN, 0, 0, 0, 0, 0, 0, GREEN},

  {0, 0, 0, GREEN, GREEN, 0, 0, 0},
  {0, 0, GREEN, 0, 0, GREEN, 0, 0},
  {0, GREEN, 0, 0, 0, 0, GREEN, 0},
  {GREEN, 0, 0, 0, 0, 0, 0, GREEN}
};


//O
uint32_t gasPatternDynamic[MLEN][NUMPIXELS] = {
  {ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE, ORANGE},
};


//P
uint32_t offLimitsPatternDynamic[MLEN][NUMPIXELS] = {
  {RED, RED, RED, RED, RED, RED, RED, RED},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {RED, RED, RED, RED, RED, RED, RED, RED},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {RED, RED, RED, RED, RED, RED, RED, RED},
};


//R
uint32_t unreadMessagePatternDynamic[MLEN][NUMPIXELS] = {
  {0, BLUE, BLUE, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, BLUE, BLUE, 0, 0, 0, 0, 0},
  {0, 0, 0, 0, 0, 0, 0, 0},
  {0, BLUE, BLUE, 0, 0, 0, 0, 0},
};



struct {
  int len;
  int pace;
  uint32_t *patternPtr;
} patternTmp;

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

void getSerialCommand() {

  if (Serial.available() > 0) {

    char inChar = Serial.read();

    switch (inChar) {
      case 'A':
        patternTmp.len = SLEN;
        patternTmp.pace = 1000;
        patternTmp.patternPtr = *farLeftPatternStatic;
        break;
      case 'B':
        patternTmp.len = SLEN;
        patternTmp.pace = 1000;
        patternTmp.patternPtr = *nearLeftPatternStatic;
        break;
      case 'C':
        patternTmp.len = SLEN;
        patternTmp.pace = 1000;
        patternTmp.patternPtr = *farRightPatternStatic;
        break;
      case 'D':
        patternTmp.len = SLEN;
        patternTmp.pace = 1000;
        patternTmp.patternPtr = *nearRightPatternStatic;
        break;
      case 'E':
        patternTmp.len = SLEN;
        patternTmp.pace = 1000;
        patternTmp.patternPtr = *farHeadUpPatternStatic;
        break;
      case 'F':
        patternTmp.len = SLEN;
        patternTmp.pace = 1000;
        patternTmp.patternPtr = *nearHeadUpPatternStatic;
        break;
      case 'G':
        patternTmp.len = SLEN;
        patternTmp.pace = 1000;
        patternTmp.patternPtr = *farBackPatternStatic;
        break;
      case 'H':
        patternTmp.len = SLEN;
        patternTmp.pace = 1000;
        patternTmp.patternPtr = *nearBackPatternStatic;
        break;
      case 'K':
        patternTmp.len = DLEN;
        patternTmp.pace = 250;
        patternTmp.patternPtr = *goLeftPatternDynamic;
        break;
      case 'L':
        patternTmp.len = DLEN;
        patternTmp.pace = 250;
        patternTmp.patternPtr = *goRightPatternDynamic;
        break;
      case 'M':
        patternTmp.len = DLEN;
        patternTmp.pace = 250;
        patternTmp.patternPtr = *goAheadPatternDynamic;
        break;
      case 'N':
        patternTmp.len = DLEN;
        patternTmp.pace = 250;
        patternTmp.patternPtr = *goBackPatternDynamic;
        break;
      case 'O':
        patternTmp.len = MLEN;
        patternTmp.pace = 250;
        patternTmp.patternPtr = *gasPatternDynamic;
        break;
      case 'P':
        patternTmp.len = MLEN;
        patternTmp.pace = 250;
        patternTmp.patternPtr = *offLimitsPatternDynamic;
        break;
      case 'R':
        patternTmp.len = MLEN;
        patternTmp.pace = 250;
        patternTmp.patternPtr = *unreadMessagePatternDynamic;
        break;

      case 'Z':
        flashlightFlag = !flashlightFlag;
        break;

      default:
        return;
    }

    Serial.println("char is " + String(inChar));

    int arrayBound = patternTmp.len * NUMPIXELS;

    for (int i = 0; i < arrayBound; i += 8) {
      
      for (int j = 0; j < NUMPIXELS; j++) {
        uint32_t currentColor = *(patternTmp.patternPtr + j + i);
        
//        if(currentColor == 0){
//            if(flashlightFlag){
//              currentColor = pixels.Color(255, 255, 255);
//            }
//        }
        
        pixels.setPixelColor(j, currentColor);
      }

      pixels.show();
      delay(patternTmp.pace);
    }

//    if(flashlightFlag){
//      
//      
//    }

    pixels.clear();
    pixels.show();
  }

}

void setup() {
  Serial.begin(9600);
  Serial1.begin(9600);
  // Wait until serial port is opened
  while (!Serial) {
    delay(1);
  }

  vcnl4040Init();
  pixels.begin();
}

void loop() {
  getSerialCommand();
//  teensySerialEvent();
//  teensyPrint();
}

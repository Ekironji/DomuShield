#include <dht.h>

#include <adk.h>

#define RCVSIZE 4

//
// Accessory descriptor. It's how Arduino identifies itself in Android.
//
char accessoryName[] = "DomuShield";
char manufacturer[] = "UDOO";
char model[] = "UDOODomuShield";

char versionNumber[] = "1.0";
char serialNumber[] = "1";
char url[] = "http://www.giovanniburresi.com";

USBHost Usb;
ADK adk(&Usb, manufacturer, model, accessoryName, versionNumber, url, serialNumber);
uint8_t buffer[RCVSIZE];
uint32_t readBytes = 0;

//
// DomuShield
//
const int RELAY_PINS[]  = {12, 13};
const int LED_STRIP_PINS[][3]  = {{11, 10, 9}};
const int SENSOR_PIN = 4;
const int LIGHT_SENSOR_PIN = A0;
const int REF_PIN_3V3 = 8;

const int LED_STARTING_PIN = 2;

const int RED = 0;
const int GREEN = 1;
const int BLUE = 2;

const int FIXED_COLOR   = 0x0;
const int FADING_COLOR  = 0x1;
const int BLINK_COLOR   = 0x2;
const int RAINBOW_COLOR = 0x3;

const int OP_CODE_MASK       = 0xf0000000;
const int MAIN_OP_CODE_MASK  = 0xc0000000;
const int SUB_OP_CODE_MASK   = 0x30000000;
const int ID_CODE_MASK       = 0x0f000000;
    
const int CMD_MASK     = 0xff000000;
const int RED_MASK     = 0x00ff0000;
const int GREEN_MASK   = 0x0000ff00;
const int BLUE_MASK    = 0x000000ff;
   
const int PAYLOAD_MASK = 0x00ffffff;
   
   // offsets
const int MAIN_OP_OFFSET = 30;
const int SUB_OP_OFFSET  = 28;
const int OP_OFFSET      = 28;
const int ID_OFFSET      = 24;
const int CMD_OFFSET     = 24;
const int RED_OFFSET     = 16;
const int GREEN_OFFSET   = 8;
const int BLUE_OFFSET    = 0;
      
   // MAIN op_codes
const int REQUEST_MSG   = 0x0;
const int RELAY_MSG     = 0x1;
const int STRIP_MSG     = 0x2;
const int VIDEO_MSG     = 0x3;
   
   // SUB op_codes ///////////////////////
const int REQUEST_IP_DISCOVERY_MSG   = 0x0;
const int REQUEST_SERVICE_LIST_MSG   = 0x1;
const int REQUEST_GENERIC_MSG        = 0x2;

const int PLAY_LED_DIRECT_COLOR  = 0x0;
const int PLAY_LED_FADING_COLOR  = 0x1;
const int PLAY_LED_BLINK_COLOR   = 0x2;
const int PLAY_LED_RAINBOW_COLOR = 0x3;

const int TURN_OFF_RELAY  = 0x0;
const int TURN_ON_RELAY   = 0x1;
const int SWITCH_RELAY    = 0x2;

// Executing
boolean relayState[] = {false, false};
int ledStrips[][3]   = {{100, 0, 0}};
int ledStripState[]  = {FIXED_COLOR};



uint8_t luminosity  = 0;
uint8_t humidity    = 0;
uint8_t temperature = 0;

dht DHT;

void setup() {
  Serial.begin(115200);
  for(int i=8; i<=13; i++){
    pinMode(i, OUTPUT);
  }
  // 3v3ref  
  digitalWrite(REF_PIN_3V3, HIGH);  
//  digitalWrite(7, LOW);
//  digitalWrite(9, LOW);
      
  Serial.println("Ready to listen!");
  delay(2000);
}

void loop() {
  // put your main code here, to run repeatedly:
    Usb.Task();

    if (adk.isReady()){
        adk.read(&readBytes, RCVSIZE, buffer);
        if (readBytes == 4){
            decodeCommand(0);
            // printBytesToBit(buffer);
        }

        readSensors();
   
    }
    
   
    
  for(int i=0; i<2; i++){
    if(relayState[i])
      digitalWrite((int) RELAY_PINS[i] , HIGH );
    else  
      digitalWrite((int) RELAY_PINS[i] , LOW );
  }
  
  for(int i=0; i<1; i++){    
    switch(ledStripState[i]){
      case FIXED_COLOR:
        analogWrite((int) LED_STRIP_PINS[i][RED]  , 255 - ledStrips[i][RED] );
        analogWrite((int) LED_STRIP_PINS[i][GREEN], 255 - ledStrips[i][GREEN] );
        analogWrite((int) LED_STRIP_PINS[i][BLUE] , 255 - ledStrips[i][BLUE] );
        break;
    }
  }
  
}


void writeToAdk(char textToSend[]) {
    adk.write(sizeof(textToSend), (uint8_t*)textToSend);
}

void printBytesToBit (uint8_t *integ){
  for(int i=0; i<4; i++){
    Serial.print(" ");
    Serial.print(integ[i], BIN );
  }
  Serial.println();
}


void readSensors(){ 
  temperature = (uint8_t) 20; 
  humidity    = (uint8_t) 50; 

  luminosity = map(analogRead(LIGHT_SENSOR_PIN), 0 , 1024, 255, 0);
  
  DHT.read11(SENSOR_PIN);
   
  uint8_t b[] = "123";
  b[0] = DHT.temperature;
  b[1] = DHT.humidity;
  b[2] = luminosity;
  
  Serial.print("temp: ");
  Serial.print(temperature); 
  Serial.print("  hum: ");
  Serial.print(humidity); 
  Serial.print(  "lum: ");
  Serial.print(luminosity);
     
  Serial.print("  -  b0: ");
  Serial.print(b[0]); 
  Serial.print("  b1: ");
  Serial.print(b[1]); 
  Serial.print("  b2: ");
  Serial.println(b[2]);
   
  adk.write(3, b);
}

//
int decodeCommand(uint32_t msg){
  
//  int opCode    = (msg & MAIN_OP_CODE_MASK) >> MAIN_OP_OFFSET;
//  int subOpCode = (msg & SUB_OP_CODE_MASK)  >> SUB_OP_OFFSET;
//  int idCode    = (msg & ID_CODE_MASK)  >> ID_OFFSET;
//  int payload[3];
//  
//  payload[0] = (msg & RED_MASK)   >> RED_OFFSET;
//  payload[1] = (msg & GREEN_MASK) >> GREEN_OFFSET;
//  payload[2] = (msg & BLUE_MASK)  >> BLUE_OFFSET;

  msg = ((uint32_t)buffer[0] << 24);
  uint32_t opCode    = (msg & MAIN_OP_CODE_MASK) >> MAIN_OP_OFFSET;
  uint32_t subOpCode = (msg & SUB_OP_CODE_MASK)  >> SUB_OP_OFFSET;
  uint32_t idCode    = (msg & ID_CODE_MASK)      >> ID_OFFSET;
  uint32_t payload[3];
  
  
  Serial.print("OP:  ");
  Serial.print(opCode, BIN);
  Serial.print("    SUBOP:  ");
  Serial.print(subOpCode, BIN);
  Serial.print("    ID: ");
  Serial.print(idCode, BIN);
  Serial.print("\n ");
  
  payload[0] = buffer[1];
  payload[1] = buffer[2];
  payload[2] = buffer[3];

  switch(opCode){
  case REQUEST_MSG:
    switch(subOpCode){
      case REQUEST_IP_DISCOVERY_MSG:
        break;
      case REQUEST_SERVICE_LIST_MSG:
        break;
      case REQUEST_GENERIC_MSG:
        break;
    }
    break;
    
  case STRIP_MSG:
    switch(subOpCode){
      case PLAY_LED_DIRECT_COLOR:
        playLedColor(idCode, payload[0], payload[1], payload[2]);
        break;
    }
    break;
    
  case RELAY_MSG:
    switch(subOpCode){
      case TURN_OFF_RELAY:
        turnOffRelay(idCode);
        break;
      case TURN_ON_RELAY:
        turnOnRelay(idCode);
        break;
      case SWITCH_RELAY:
        switchRelay(idCode);
        break;
    }
    break;

  }
  return 0;
}


int switchRelay(int relay){
  if(relayState[relay] == true)
    relayState[relay] = false;
  else
    relayState[relay] = true;
    
  return 0;
}

int turnOnRelay(int relay){
  relayState[relay] = true;    
  return 0;
}

int turnOffRelay(int relay){
  relayState[relay] = false;    
  return 0;
}

int getLightIntensity(){
  return 33;
}

int getTemperature(){
  return 11;
}

int getHumidity(){
  return 22;
}

int playLedColor(int strip, int r, int g, int b){  
  ledStripState[strip] = FIXED_COLOR;
  
  ledStrips[strip][RED]   = r;
  ledStrips[strip][GREEN] = g;
  ledStrips[strip][BLUE]  = b;  

  return 0;
}

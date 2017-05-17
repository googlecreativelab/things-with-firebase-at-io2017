// Adafruit_NeoMatrix example for tiled NeoPixel matrices.  Scrolls
// 'Howdy' across three 10x8 NeoPixel grids that were created using
// NeoPixel 60 LEDs per meter flex strip.

#include <Adafruit_GFX.h>
#include <Adafruit_NeoMatrix.h>
#include <Adafruit_NeoPixel.h>
#ifndef PSTR
 #define PSTR // Make Arduino Due happy
#endif

#define PIN 6 //NeoPixel Pin

Adafruit_NeoMatrix matrix = Adafruit_NeoMatrix(8, 8, 5, 1, PIN,
  NEO_TILE_TOP   + NEO_TILE_LEFT   + NEO_TILE_ROWS   + NEO_TILE_PROGRESSIVE +
  NEO_MATRIX_TOP + NEO_MATRIX_LEFT + NEO_MATRIX_ROWS + NEO_MATRIX_ZIGZAG,
  NEO_GRB + NEO_KHZ800);

  String input;
  boolean stringUsed = false;
  char c = "";
  int fadeBrightness = 0;
  int fadeRate = 5;
  int topEndFade = 255;
  bool fadeState = false;

void setup() {
  matrix.begin();
  Serial1.begin(9600);
  Serial.begin(9600);
  input.reserve(200);
  input = "";
}

int x    = matrix.width();
int pass = 0;

void loop() {

  
  // if there's any serial available, read it:
  while (Serial1.available()) {
    delay(10);
    c = Serial1.read();
   }

   // handle any fading
  if (fadeBrightness == 0){
    fadeState = false;
  }else if (fadeBrightness == topEndFade){
    fadeState = true;
  }

  if (fadeState == false){
    fadeBrightness ++;
  }

  else if (fadeState == true){
    fadeBrightness --;
  }
    

  // actually do something with the serial that comes in
    if (c == 'a'){
      attract();
    }
    else if (c == 'j'){
      joinGame();     
    }
    else if (c == 'c'){
      startCount();
    }
    else if (c == 'g'){
      startGame();     
    }
    else if (c == 't'){
      endCount();
    }
    else if (c == 'w'){
      gameWinner();
    }
    else if (c == 'C'){
      correct();
    }
    else if (c == 'l'){
      gameLoser();
    }
    else{      
      matrix.setBrightness(0);
      matrix.show(); 
  }  
  
}


void attract(){
      matrix.fillScreen(matrix.Color(255,209,25));
      matrix.setBrightness(fadeBrightness);
      matrix.show();
}

void joinGame(){ 
    Serial.println("joinGame");
    matrix.setBrightness(255);    
    matrix.fillScreen(matrix.Color(255,209,25));
    matrix.show();
    delay(500);
}

void startCount(){
      matrix.fillScreen(matrix.Color(0,255,0));
      matrix.setBrightness(255);
      matrix.show(); 
      delay(200);
      
      matrix.fillScreen(matrix.Color(0,0,0));
      matrix.setBrightness(255);
      matrix.show(); 
      delay(200);
}

void startGame(){
      matrix.fillScreen(matrix.Color(255,209,25));
      matrix.setBrightness(fadeBrightness);
      matrix.show();
}

void endCount(){
      matrix.fillScreen(matrix.Color(255,0,0));
      matrix.setBrightness(255);
      matrix.show(); 
      delay(200);
      matrix.fillScreen(matrix.Color(0,0,0));
      matrix.setBrightness(255);
      matrix.show(); 
      delay(200);    
}

void gameWinner(){
    for (int i=0; i<3; i++){
      matrix.fillScreen(matrix.Color(0,255,0));
      matrix.setBrightness(255);
      matrix.show(); 
      delay(200);
      matrix.fillScreen(matrix.Color(0,0,0));
      matrix.setBrightness(255);
      matrix.show(); 
      delay(200);
  }
}

void gameLoser(){
    for (int i=0; i<3; i++){
      matrix.fillScreen(matrix.Color(255,0,0));
      matrix.setBrightness(255);
      matrix.show(); 
      delay(200);
      matrix.fillScreen(matrix.Color(0,0,0));
      matrix.setBrightness(255);
      matrix.show(); 
      delay(200);
  }
}

void correct(){ 
    matrix.setBrightness(255);    
    matrix.fillScreen(matrix.Color(0,255,0));
    matrix.show();
    delay(500);
  }

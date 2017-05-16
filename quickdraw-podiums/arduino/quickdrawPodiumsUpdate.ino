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

// MATRIX DECLARATION:
// Parameter 1 = width of EACH NEOPIXEL MATRIX (not total display)
// Parameter 2 = height of each matrix
// Parameter 3 = number of matrices arranged horizontally
// Parameter 4 = number of matrices arranged vertically
// Parameter 5 = pin number (most are valid)
// Parameter 6 = matrix layout flags, add together as needed:
//   NEO_MATRIX_TOP, NEO_MATRIX_BOTTOM, NEO_MATRIX_LEFT, NEO_MATRIX_RIGHT:
//     Position of the FIRST LED in the FIRST MATRIX; pick two, e.g.
//     NEO_MATRIX_TOP + NEO_MATRIX_LEFT for the top-left corner.
//   NEO_MATRIX_ROWS, NEO_MATRIX_COLUMNS: LEDs WITHIN EACH MATRIX are
//     arranged in horizontal rows or in vertical columns, respectively;
//     pick one or the other.
//   NEO_MATRIX_PROGRESSIVE, NEO_MATRIX_ZIGZAG: all rows/columns WITHIN
//     EACH MATRIX proceed in the same order, or alternate lines reverse
//     direction; pick one.
//   NEO_TILE_TOP, NEO_TILE_BOTTOM, NEO_TILE_LEFT, NEO_TILE_RIGHT:
//     Position of the FIRST MATRIX (tile) in the OVERALL DISPLAY; pick
//     two, e.g. NEO_TILE_TOP + NEO_TILE_LEFT for the top-left corner.
//   NEO_TILE_ROWS, NEO_TILE_COLUMNS: the matrices in the OVERALL DISPLAY
//     are arranged in horizontal rows or in vertical columns, respectively;
//     pick one or the other.
//   NEO_TILE_PROGRESSIVE, NEO_TILE_ZIGZAG: the ROWS/COLUMS OF MATRICES
//     (tiles) in the OVERALL DISPLAY proceed in the same order for every
//     line, or alternate lines reverse direction; pick one.  When using
//     zig-zag order, the orientation of the matrices in alternate rows
//     will be rotated 180 degrees (this is normal -- simplifies wiring).
//   See example below for these values in action.
// Parameter 7 = pixel type flags, add together as needed:
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 pixels)
//   NEO_GRB     Pixels are wired for GRB bitstream (v2 pixels)
//   NEO_KHZ400  400 KHz bitstream (e.g. FLORA v1 pixels)
//   NEO_KHZ800  800 KHz bitstream (e.g. High Density LED strip)

// Example with three 10x8 matrices (created using NeoPixel flex strip --
// these grids are not a ready-made product).  In this application we'd
// like to arrange the three matrices side-by-side in a wide display.
// The first matrix (tile) will be at the left, and the first pixel within
// that matrix is at the top left.  The matrices use zig-zag line ordering.
// There's only one row here, so it doesn't matter if we declare it in row
// or column order.  The matrices use 800 KHz (v2) pixels that expect GRB
// color data.
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

    Serial.print(">>>");
    Serial.print(c);
    Serial.println("<<<");

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
    
    Serial.println(fadeBrightness);
  
    if (c == 'a'){
      Serial.println("attract");
      attract();
    }
    else if (c == 'j'){
      Serial.println("joinGame");
      joinGame();     
    }
    else if (c == 'c'){
      Serial.println("startCount");
      startCount();
    }
    else if (c == 'g'){
      Serial.println("startGame");
      startGame();     
    }
    else if (c == 't'){
      Serial.println("timeWarning");
      endCount();
    }
    else if (c == 'w'){
      Serial.println("winner!!!!!");
      gameWinner();
    }
    else if (c == 'C'){
      Serial.println("Correct!!!");
      correct();
    }
    else if (c == 'l'){
      Serial.println("Oh No!!!");
      gameLoser();
    }
    else{      
      Serial.println("else");
      matrix.setBrightness(0);
      matrix.show(); 
  }  
  
}


void attract(){
    //   for (int x=0; x<=255; x++){
    //     matrix.fillScreen(matrix.Color(255,209,25));
    //     matrix.setBrightness(x);
    //     matrix.show();
    // }
    //   for (int x=255; x>=0; x--){
    //     matrix.fillScreen(matrix.Color(255,209,25));
    //     matrix.setBrightness(x);
    //     matrix.show();
    // }

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
//    matrix.setBrightness(255);
//    matrix.fillScreen(matrix.Color(255,209,25));    
//    matrix.show();  
//   delay(500);   

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

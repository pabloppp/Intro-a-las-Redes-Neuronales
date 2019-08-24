class Obstacle{
  float size;
  float y;
  float x = int(random(0, tracks*1)) * width / tracks + (width / tracks / 2);
  float spawnTime = millis();
  
  Obstacle(int track, int size){
    this.size = size * 0.75;
    x = track * width / tracks + (width / tracks / 2);
    y = -size*0.75;
  }
  
  void draw(){
    noStroke();
    fill(255, 82, 82);
    rect(x-size/2, y-(size*1.5)/2, size, size*1.5, 16);
    fill(0, 0, 0, 50);
    rect(x-size/2+4, y-size/2+12, size-8, size, 16);
    
    //update position
    float elapsedTime = millis() - spawnTime;
    spawnTime = millis();
    y += elapsedTime / 5;
  } 
}

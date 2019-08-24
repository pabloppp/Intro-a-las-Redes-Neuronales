class Sensor{
  int x;
  int interpx;
  int y;
  boolean detected = false;
  
  
  Sensor(int x, int y){
    this.x = x;
    this.interpx = x;
    this.y = y;
  }
  
  void draw(){
    noFill();
    if(detected) stroke(255, 0, 0);
    else stroke(0, 255, 0, 100);
    strokeWeight(2);
    ellipse(interpx, y, 8, 8);
    
    if (abs(interpx - x) <= 20){
      interpx = x;
    } else if(interpx < x){
      interpx += 20;
    } else if(interpx > x){
      interpx -= 20;
    }
  }
  
  void detect(ArrayList<Obstacle> obstacles){
    detected = false;
    for(int i = obstacles.size() - 1; i>=0; i--){
      if((abs(obstacles.get(i).x - x) < 8 
          && obstacles.get(i).y + obstacles.get(i).size*0.75 > y  
          && obstacles.get(i).y - obstacles.get(i).size*0.75 < y) 
          || x < 0 || x > width){
        detected = true;
      }
    }
  }
}

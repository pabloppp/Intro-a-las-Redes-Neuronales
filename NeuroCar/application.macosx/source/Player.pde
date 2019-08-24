class Player{
  int y = height - 100;
  int x = int(width / tracks * (int(tracks / 2)+0.5));
  int interpx = x;
  
  float size;
  
  Sensor[] sensors;
  
  Player(int size){
    this.size = size * 0.75;
    
    int sensorRange = 180;
    
    sensors = new Sensor[] { 
      new Sensor(x-width / tracks, y-sensorRange),
      new Sensor(x, y-sensorRange),
      new Sensor(x+width / tracks, y-sensorRange) 
    };
  }
  
  void draw(){
    noStroke();
    fill(66, 165, 245);
    rect(interpx-size/2, y-(size*1.5)/2, size, size*1.5, 16);
    fill(0, 0, 0, 50);
    rect(interpx-size/2+4, y-size/2+12, size-8, size, 16);
    
    if (abs(interpx - x) <= 20){
      interpx = x;
    } else if(interpx < x){
      interpx += 20;
    } else if(interpx > x){
      interpx -= 20;
    }
    
    for(int i = 0; i<sensors.length;i++){     
      sensors[i].draw();
      line(interpx, y-size*0.75, sensors[i].interpx, sensors[i].y);
    }
  } 
  
  void move(int direction){
    if(direction == -1 && (player.x - width / tracks) > 0){
      x -= width / tracks;   
      for(int i = 0; i<sensors.length;i++){
        sensors[i].x -= width / tracks;
      }
    } else if(direction == 1 && (player.x + width / tracks) < width){
      x += width / tracks;
      for(int i = 0; i<sensors.length;i++){
        sensors[i].x += width / tracks;
      }
    }
  }
  
  boolean collides(ArrayList<Obstacle> obstacles){
    for(int i = 0; i<sensors.length;i++){     
      sensors[i].detect(obstacles);
    }
    
    for(int i = obstacles.size() - 1; i>=0; i--){
      if(abs(obstacles.get(i).x - x) < 8 && obstacles.get(i).y > y - size*1.4 && obstacles.get(i).y < y + size*1){
        return true;
      }
    }
    return false;
  }
}

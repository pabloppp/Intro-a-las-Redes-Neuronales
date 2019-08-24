import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.net.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class NeuroCar extends PApplet {

 

Player player;

ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

int tracks = 3;
int counter = millis();
int record_counter = millis();
int socket_counter = millis();
int score = 0;
int last_action = 0;
int[] last_sensor_data = new int[]{0, 0, 0};
boolean recording = false;
ArrayList<int[]> records = new ArrayList<int[]>();
Client myClient; 
boolean sockets = true;
boolean automate = false;
String payload = "";

int socket_update_rate = 50;

public void setup(){ 
  
  player = new Player(width / tracks);
  
  if(sockets) myClient = new Client(this, "127.0.0.1", 5204);
}


public void draw(){
  background(108, 108, 108);
  for(int i = -1; i < 2 + height / 32; i++){
    for(int j=1; j<tracks; j++){
      stroke(241, 241, 241, 50);
      strokeWeight(4);
      float start = (millis() / 4) % 32;
      line(j * (width / tracks), i*32 + start, j * (width / tracks), i*32+16 + start);
    }
  }
  
  if(myClient != null && millis() - socket_counter > socket_update_rate){    
    if(automate){
      try{
        int dataIn = PApplet.parseInt(myClient.readString());
        if(dataIn != 0){
          player.move(dataIn);
          last_action = dataIn;
        }
        // println(payload+" >>> "+dataIn);
      } catch(Exception e){
        // Do nothing
      }
    }
  }
  
  for(int i = obstacles.size() - 1; i>=0; i--){
    obstacles.get(i).draw();
    if(obstacles.get(i).y > height + 100){
      obstacles.remove(i);
      score += 1;
    }
  }
  
  player.draw();
  if(player.collides(obstacles)){
    score = 0;
    obstacles = new ArrayList<Obstacle>();
  }
  
  if(millis() - counter > 1600){
    counter = millis();
    
    ArrayList<Integer> availableTracks = new ArrayList<Integer>();
    for(int i = 0; i<tracks; i++){
      availableTracks.add(i);
    }
    
    int track = PApplet.parseInt(random(0, availableTracks.size()));
    track = availableTracks.get(track);
    availableTracks.remove(track);
    obstacles.add(new Obstacle(track, width / tracks));
    if(random(0, 100) > 75){ 
      track = PApplet.parseInt(random(0, availableTracks.size()));
      track = availableTracks.get(track);
      obstacles.add(new Obstacle(track, width / tracks));
    }
  }
  
  if(automate && myClient != null && millis() - socket_counter > socket_update_rate){
    socket_counter = millis();
    payload = PApplet.parseInt(player.sensors[0].detected)+" "+PApplet.parseInt(player.sensors[1].detected)+" "+PApplet.parseInt(player.sensors[2].detected);
    myClient.write(payload);      
  }
  
  if(recording && millis() - record_counter > 100){
    record_counter = millis();
    records.add(new int[]{
      last_sensor_data[0], 
      last_sensor_data[1], 
      last_sensor_data[2],
      last_action
    });
    last_action = 0;
    last_sensor_data[0] = PApplet.parseInt(player.sensors[0].detected);
    last_sensor_data[1] = PApplet.parseInt(player.sensors[1].detected);
    last_sensor_data[2] = PApplet.parseInt(player.sensors[2].detected);
  }
  
  fill(255);
  text(score, 16, 16);
  if(recording) {
    fill(255, 0, 0);
    noStroke();
    ellipse(width-16, 16, 14, 14);
    text(records.size(), 16, 32);
  }
  
  if(automate) {
    text("Auto", 16, 48);  
  }
}

public void keyPressed(){
  if(key == 'a'){
    player.move(-1);
    last_action = -1;
  } else if(key == 'd'){
    player.move(1);
    last_action = 1;
  }
  
  if(key == 't'){ 
    automate = !automate;
    socket_counter = millis();
  }
  
  if(key == 'r'){
    if(!recording){ 
      recording = true;
      last_action = 0;
      records = new ArrayList<int[]>();
      record_counter = millis();
    } else {
      recording = false;
      println(records.size());
      
      // clean duplicated records
      for (int i=records.size()-2; i>=0; i--){
        int[] rec = records.get(i+1);
        int[] rec_prev = records.get(i);
        if(rec[0] == rec_prev[0] && rec[1] == rec_prev[1] && rec[2] == rec_prev[2]){
          records.remove(i);
        }
      }
      
      println(records.size());
      
      try{            
        PrintWriter pr = createWriter("records.txt");    
        for (int i=0; i<records.size() ; i++){
          String line = "";
          for(int j = 0; j<records.get(i).length; j++){
            line += str(records.get(i)[j])+" ";
          }
          pr.println(line);
        }
        pr.flush();
        pr.close();
      } catch(Exception e){
        print(e);
        // DO NOTHING
      }
    }
  }
}
class Obstacle{
  float size;
  float y;
  float x = PApplet.parseInt(random(0, tracks*1)) * width / tracks + (width / tracks / 2);
  float spawnTime = millis();
  
  Obstacle(int track, int size){
    this.size = size * 0.75f;
    x = track * width / tracks + (width / tracks / 2);
    y = -size*0.75f;
  }
  
  public void draw(){
    noStroke();
    fill(255, 82, 82);
    rect(x-size/2, y-(size*1.5f)/2, size, size*1.5f, 16);
    fill(0, 0, 0, 50);
    rect(x-size/2+4, y-size/2+12, size-8, size, 16);
    
    //update position
    float elapsedTime = millis() - spawnTime;
    spawnTime = millis();
    y += elapsedTime / 5;
  } 
}
class Player{
  int y = height - 100;
  int x = PApplet.parseInt(width / tracks * (PApplet.parseInt(tracks / 2)+0.5f));
  int interpx = x;
  
  float size;
  
  Sensor[] sensors;
  
  Player(int size){
    this.size = size * 0.75f;
    
    int sensorRange = 180;
    
    sensors = new Sensor[] { 
      new Sensor(x-width / tracks, y-sensorRange),
      new Sensor(x, y-sensorRange),
      new Sensor(x+width / tracks, y-sensorRange) 
    };
  }
  
  public void draw(){
    noStroke();
    fill(66, 165, 245);
    rect(interpx-size/2, y-(size*1.5f)/2, size, size*1.5f, 16);
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
      line(interpx, y-size*0.75f, sensors[i].interpx, sensors[i].y);
    }
  } 
  
  public void move(int direction){
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
  
  public boolean collides(ArrayList<Obstacle> obstacles){
    for(int i = 0; i<sensors.length;i++){     
      sensors[i].detect(obstacles);
    }
    
    for(int i = obstacles.size() - 1; i>=0; i--){
      if(abs(obstacles.get(i).x - x) < 8 && obstacles.get(i).y > y - size*1.4f && obstacles.get(i).y < y + size*1){
        return true;
      }
    }
    return false;
  }
}
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
  
  public void draw(){
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
  
  public void detect(ArrayList<Obstacle> obstacles){
    detected = false;
    for(int i = obstacles.size() - 1; i>=0; i--){
      if((abs(obstacles.get(i).x - x) < 8 
          && obstacles.get(i).y + obstacles.get(i).size*0.75f > y  
          && obstacles.get(i).y - obstacles.get(i).size*0.75f < y) 
          || x < 0 || x > width){
        detected = true;
      }
    }
  }
}
  public void settings() {  size(300, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "NeuroCar" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

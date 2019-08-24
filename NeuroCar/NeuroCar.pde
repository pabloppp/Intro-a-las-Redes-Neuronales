import processing.net.*; 

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

void setup(){ 
  size(300, 600);
  player = new Player(width / tracks);
  
  if(sockets) myClient = new Client(this, "127.0.0.1", 5204);
}


void draw(){
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
        int dataIn = int(myClient.readString());
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
    
    int track = int(random(0, availableTracks.size()));
    track = availableTracks.get(track);
    availableTracks.remove(track);
    obstacles.add(new Obstacle(track, width / tracks));
    if(random(0, 100) > 75){ 
      track = int(random(0, availableTracks.size()));
      track = availableTracks.get(track);
      obstacles.add(new Obstacle(track, width / tracks));
    }
  }
  
  if(automate && myClient != null && millis() - socket_counter > socket_update_rate){
    socket_counter = millis();
    payload = int(player.sensors[0].detected)+" "+int(player.sensors[1].detected)+" "+int(player.sensors[2].detected);
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
    last_sensor_data[0] = int(player.sensors[0].detected);
    last_sensor_data[1] = int(player.sensors[1].detected);
    last_sensor_data[2] = int(player.sensors[2].detected);
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

void keyPressed(){
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

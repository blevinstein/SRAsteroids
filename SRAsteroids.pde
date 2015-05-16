// GOAL: demonstrate space/time contractions, and behavior of relativistic spacetime

ArrayList<Timeline> timelines;
Event now = new Event(0, 0, 0);

float dt = 1;
float c = 10;

void setup() {
  size(640, 480);
  
  resetMatrix();
  
  ellipseMode(RADIUS);
  
  timelines = new ArrayList<Timeline>();
}

void draw() {
  clear();
  
  // Add random objects
  if (random(1) < 0.3) {
    //timelines.add(new StaticTimeline(random(-width/2, width/2), random(-height/2, height/2)));
    //timelines.add(new ConstantTimeline(-width/2, random(-height/2, height/2), now.t, random(0.1, 1), random(-0.5, 0.5)));
    timelines.add(
        new ConstantTimeline(
            now.x + random(-width/2, width/2), now.y + random(-height/2, height/2), now.t,
            random(-0.7, 0.7), random(-0.7, 0.7)));
  }
  
  // Calculate beta
  float bx = (mouseX - width/2f) / (width/2) * 0.7;
  float by = (mouseY - height/2f) / (height/2) * 0.7;

  // Show the observer
  noFill();
  stroke(255);
  ellipse(width/2, height/2, 5, 5);

  // Show the objects
  for (Timeline timeline : timelines) {
    // TODO: use intersection on light cone instead of normal time
    Event event = timeline.at(now.t).relativeTo(now);
    Event image = lorentz(event, bx, by);
    // NOTE: use red = future, blue = past
    noStroke();
    fill(constrain(255 + image.t, 0, 255),
        constrain(255 - abs(image.t), 0, 255),
        constrain(255 - image.t, 0, 255));
    // TODO: use box() instead of ellipse(), 3d to allow rotation into time
    ellipse(image.x + width/2, image.y + height/2, 2, 2);
  }
  
  // TODO: replace (now : Event) with (self : Timeline)
  now = now.advance(bx * c, by * c, dt);
}

/// Clear the screen
void clear() {
  fill(0);
  stroke(0);
  rect(0, 0, width, height);
}

// given (bx, by) = beta = v / c
// returns new spacetime coordinates for an event after transformation
Event lorentz(Event e, float bx, float by) {
  if (bx * bx + by * by > 1) throw new IllegalArgumentException("|beta| > 1");
  
  float beta_sq = bx * bx + by * by;
  float gamma = 1 / sqrt(1 - beta_sq);
  float x = - e.t * gamma * bx * c
            + e.x * (1 + (gamma - 1) * (bx * bx) / beta_sq)
            + e.y * ((gamma - 1) * (bx * by) / beta_sq);
  float y = - e.t * gamma * by * c
            + e.x * ((gamma - 1) * (bx * by) / beta_sq)
            + e.y * (1 + (gamma - 1) * (by * by) / beta_sq);
  float t = e.t * gamma
          - e.x * gamma * bx
          - e.y * gamma * by;
  return new Event(x, y, t);        
}

interface Timeline {
  Event at(float t);
  // TODO: Event relativeTo(Event e, float t, float vx, float vy);
}

class ConstantTimeline implements Timeline{
  float ix;
  float iy;
  float it;
  float vx;
  float vy;
  
  ConstantTimeline(float ix, float iy, float it, float vx, float vy) {
    this.ix = ix;
    this.iy = iy;
    this.it = it;
    this.vx = vx;
    this.vy = vy;
  }
  
  Event at(float t) {
    return new Event(ix + vx * (t - it), iy + vy * (t - it), t);
  }
}

// TODO: implement moving timelines
// TODO: implement arbitrary timelines?
class StaticTimeline implements Timeline {
  float x;
  float y;
  
  StaticTimeline(float x, float y) {
    this.x = x;
    this.y = y;
  }
  
  Event at(float t) {
    return new Event(x, y, t);
  }
}

class Event {
  float x;
  float y;
  float t;
  
  Event(float x, float y, float t) {
    this.x = x;
    this.y = y;
    this.t = t;
  }
  
  Event relativeTo(Event other) {
    return new Event(this.x - other.x, this.y - other.y, this.t - other.t);
  }
  
  Event advance(float dt) {
    return advance(0, 0, dt);
  }
  Event advance(float dx, float dy, float dt) {
    return new Event(this.x + dx, this.y + dy, this.t + dt);
  }
}

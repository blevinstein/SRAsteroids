// GOAL: demonstrate space/length contractions, and behavior of relativistic spacetime
// SUCCESS: visualized length contraction

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
    Event event = concurrentWith(timeline, now, bx, by);
    Event image = lorentz(event.relativeTo(now), bx, by);
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
  checkBeta(bx, by);
  
  float beta_sq = bx * bx + by * by;
  float gamma = 1 / sqrt(1 - beta_sq);

  float x = - e.t * gamma * bx * c
            + e.x * (1 + (gamma - 1) * (bx * bx) / beta_sq)
            + e.y * ((gamma - 1) * (bx * by) / beta_sq);
  float y = - e.t * gamma * by * c
            + e.x * ((gamma - 1) * (bx * by) / beta_sq)
            + e.y * (1 + (gamma - 1) * (by * by) / beta_sq);
  float t = e.t * gamma
          - e.x * gamma * bx / c
          - e.y * gamma * by / c;
  return new Event(x, y, t);
}

// @return e such that lorentz(e.relativeTo(observer), bx, by).t = 0
// NOTE: finds a solution using bisection method
Event concurrentWith(Timeline timeline, Event observer, float bx, float by) {
  checkBeta(bx, by);

  float beta_sq = bx * bx + by * by;
  float gamma = 1 / sqrt(1 - beta_sq);

  // low and high guesses for time in original reference frame
  float tLow = observer.t;
  float tHigh = observer.t;

  // crude approximation for dError/dt
  // intentionally low by factor 1/2 to encourage overshoot and start bisection method
  float dError = gamma / 2;

  float eLow, eHigh;
  // start with observer.t as a guess
  eLow = eHigh = lorentz(timeline.at(observer.t).relativeTo(observer), bx, by).t;
  // adjust high and low bounds until they are valid
  while (eHigh < 0) {
    tHigh -= eHigh / dError;
    eHigh = lorentz(timeline.at(tHigh).relativeTo(observer), bx, by).t;
  }
  while (eLow > 0) {
    tLow -= eLow / dError;
    eLow = lorentz(timeline.at(tLow).relativeTo(observer), bx, by).t;
  }

  // bisection method
  int iterations = 0;
  float tol = 0.001;
  float tMid = (tLow + tHigh) / 2;
  float eMid = lorentz(timeline.at(tMid).relativeTo(observer), bx, by).t;
  while (tHigh - tLow > tol && iterations < 1000) {
    if (eMid < 0) {
      tLow = tMid;
    } else if (eMid > 0) {
      tHigh = tMid;
    } else {
      return timeline.at(tMid);
    }
    tMid = (tLow + tHigh) / 2;
    eMid = lorentz(timeline.at(tMid).relativeTo(observer), bx, by).t;
  }
  return timeline.at(tMid);
}

void checkBeta(float bx, float by) {
  if (bx * bx + by * by > 1) throw new IllegalArgumentException("|beta| > 1");
}

interface Timeline {
  Event at(float t);
}

class ConstantTimeline implements Timeline {
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
  
  Event plus(Event other) {
    return new Event(this.x + other.x, this.y + other.y, this.t + other.t);
  }

  // @return 'this' as seen by 'other' event in stationary reference frame
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

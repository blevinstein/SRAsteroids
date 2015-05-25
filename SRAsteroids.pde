// GOAL: demonstrate space/length contractions, and behavior of relativistic spacetime
// SUCCESS: visualized length contraction

ArrayList<Timeline> timelines;
Event now = new Event(0, 0, 0);

float dt = 1;

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
    Event event = timeline.concurrentWith(now, bx, by);
    Event image = SR.lorentz(event.relativeTo(now), bx, by);
    // NOTE: use red = future, blue = past
    noStroke();
    fill(constrain(255 + image.t, 0, 255),
        constrain(255 - abs(image.t), 0, 255),
        constrain(255 - image.t, 0, 255));
    // TODO: use box() instead of ellipse(), 3d to allow rotation into time
    ellipse(image.x + width/2, image.y + height/2, 2, 2);
  }
  
  // TODO: replace (now : Event) with (self : Timeline)
  now = now.advance(bx * SR.c, by * SR.c, dt);
}

/// Clear the screen
void clear() {
  fill(0);
  stroke(0);
  rect(0, 0, width, height);
}


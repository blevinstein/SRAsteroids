

public class Event {
  float x;
  float y;
  float t;
  
  public Event(float x, float y, float t) {
    this.x = x;
    this.y = y;
    this.t = t;
  }
  
  public Event plus(Event other) {
    return new Event(this.x + other.x, this.y + other.y, this.t + other.t);
  }

  // @return 'this' as seen by 'other' event in stationary reference frame
  public Event relativeTo(Event other) {
    return new Event(this.x - other.x, this.y - other.y, this.t - other.t);
  }

  public Event advance(float dt) {
    return advance(0, 0, dt);
  }
  public Event advance(float dx, float dy, float dt) {
    return new Event(this.x + dx, this.y + dy, this.t + dt);
  }
}

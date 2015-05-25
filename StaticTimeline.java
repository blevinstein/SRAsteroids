

// TODO: implement moving timelines
// TODO: implement arbitrary timelines?
public class StaticTimeline extends Timeline {
  float x;
  float y;
  
  public StaticTimeline(float x, float y) {
    this.x = x;
    this.y = y;
  }
  
  public Event at(float t) {
    return new Event(x, y, t);
  }
}

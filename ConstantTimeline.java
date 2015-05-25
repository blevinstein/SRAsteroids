

public class ConstantTimeline extends Timeline {
  float ix;
  float iy;
  float it;
  float vx;
  float vy;
  
  public ConstantTimeline(float ix, float iy, float it, float vx, float vy) {
    this.ix = ix;
    this.iy = iy;
    this.it = it;
    this.vx = vx;
    this.vy = vy;
  }
  
  public Event at(float t) {
    return new Event(ix + vx * (t - it), iy + vy * (t - it), t);
  }
}

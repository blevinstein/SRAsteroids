package com.blevinstein.sr;

/**
 * Represents a stationary position.
 */
public class StaticTimeline extends Timeline {
  public static final StaticTimeline ZERO = new StaticTimeline(0, 0);

  private final double x;
  private final double y;
  
  public StaticTimeline(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public Event at(double t) {
    return new Event(x, y, t);
  }

  public Velocity velocityAt(double t) {
    return Velocity.ZERO;
  }

  public double timeElapsed(double tStart, double tEnd) {
    return tEnd - tStart;
  }

  @Override
  public String toString() {
    return "StaticTimeline " + x + ", " + y;
  }
}

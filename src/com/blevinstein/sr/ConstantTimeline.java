package com.blevinstein.sr;

/**
 * Represents a position in another inertial frame, i.e. moving with constant velocity.
 */
public class ConstantTimeline extends Timeline {
  private Event _e;
  private Velocity _v;

  public Event e() { return _e; }
  public Velocity v() { return _v; }
  
  public ConstantTimeline(Event e, Velocity v) {
    this._e = e;
    this._v = v;
  }
  
  public Event at(double t) {
    return _v.over(t - _e.t()).plus(_e);
  }

  public Velocity velocityAt(double t) {
    return _v;
  }

  public double timeElapsed(double tStart, double tEnd) {
    return _v.gamma() * (tEnd - tStart);
  }

  @Override
  public String toString() {
    return "ConstantTimeline " + _e + " @ " + _v;
  }
}

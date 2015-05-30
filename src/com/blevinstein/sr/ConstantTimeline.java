package com.blevinstein.sr;

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
    return _v.over(t).plus(_e);
  }
}

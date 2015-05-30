package com.blevinstein.sr;

public class StaticTimeline extends Timeline {
  double x;
  double y;
  
  public StaticTimeline(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public Event at(double t) {
    return new Event(x, y, t);
  }
}

package com.blevinstein.sr;

/**
 * Wrapper class for another timeline
 * Allows you to modify the start and end times of the timeline
 */
public class LimitedTimeline extends Timeline {
  private Timeline original;
  private Event start = null;
  private Event end = null;

  public LimitedTimeline(Timeline original, Event start, Event end) {
    this.original = original;
    this.start = start;
    this.end = end;
  }

  // Passthrough implementation

  public Event at(double t) { return original.at(t); }

  public Velocity velocityAt(double t) { return original.velocityAt(t); }

  public double timeElapsed(double tStart, double tEnd) { return original.timeElapsed(tStart, tEnd); }

  public Event start() {
    if (start != null) {
      return start;
    } else {
      return original.start();
    }
  }

  public Event end() {
    if (end != null) {
      return end;
    } else {
      return original.end();
    }
  }

}

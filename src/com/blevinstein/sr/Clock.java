package com.blevinstein.sr;

/**
 * Represents a clock in SR, measures time along a spacetime interval
 */
public class Clock {
  private double localTime;
  private Event position;

  public Clock(Event position) {
    this.localTime = 0;
    this.position = position;
  }

  public double time() { return localTime; }

  public void move(Event newPosition) {
    Event offset = newPosition.minus(position);
    if (offset.t() < 0) { System.err.printf("dt negative %f", offset.t()); }
    localTime += offset.timeElapsed();
    position = newPosition;
  }
}

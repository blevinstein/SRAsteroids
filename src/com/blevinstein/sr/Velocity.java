package com.blevinstein.sr;

public class Velocity {
  private float vx;
  private float vy;
  public Velocity(float vx, float vy) {
    this.vx = vx;
    this.vy = vy;
  }

  public Event over(float t) {
    return new Event(vx * t, vy * t, t);
  }
}

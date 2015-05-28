package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

public class Velocity {
  public static final Velocity ZERO = new Velocity(0, 0);

  private float vx;
  private float vy;

  public float x() { return vx; }
  public float y() { return vy; }

  public Velocity(float vx, float vy) {
    this.vx = vx;
    this.vy = vy;

    float b = beta();
    if (b > 1) {
      this.vx = this.vx / b;
      this.vy = this.vy / b;
    }
  }

  public Event over(float t) {
    return new Event(vx * t, vy * t, t);
  }

  public Velocity times(float k) {
    return new Velocity(k * vx, k * vy);
  }

  public Velocity times(float kx, float ky) {
    return new Velocity(kx * vx, ky * vy);
  }

  public float gamma() {
    return (float) (1 / Math.sqrt(1 - beta_sq()));
  }

  public float beta() {
    return (float) Math.sqrt(beta_sq());
  }

  public float beta_sq() {
    float bx = vx / c;
    float by = vy / c;
    return bx * bx + by * by;
  }
}

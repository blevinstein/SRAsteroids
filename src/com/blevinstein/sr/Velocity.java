package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

public class Velocity {
  public static final Velocity ZERO = new Velocity(0, 0);
  public static final Velocity I = new Velocity(1, 0);
  public static final Velocity J = new Velocity(0, 1);

  private float vx;
  private float vy;

  public float x() { return vx; }
  public float y() { return vy; }

  public Velocity(float vx, float vy) {
    if (Float.isNaN(vx) || Float.isNaN(vy)) {
      throw new IllegalArgumentException();
    }
    this.vx = vx;
    this.vy = vy;
  }

  public Velocity perp() {
    return new Velocity(vy, -vx);
  }

  public Velocity checked() {
    return checked(1);
  }

  public Velocity checked(float max) {
    float b = beta();
    return b > max ? this.times(max / b) : this;
  }

  public Event over(float t) {
    return new Event(vx * t, vy * t, t);
  }

  public Velocity times(float k) {
    return new Velocity(vx * k, vy * k);
  }

  public Velocity times(float kx, float ky) {
    return new Velocity(kx * vx, ky * vy);
  }

  public Velocity div(float k) {
    return new Velocity(vx / k, vy / k);
  }

  // BROKEN
  public Velocity relativePlus(Velocity other) {
    Velocity otherNorm = other.norm();
    Velocity otherParallel = otherNorm.times(this.dot(otherNorm));
    Velocity otherPerp = other.minus(otherParallel);

    // DEBUG
    //System.out.println("otherNorm " + otherNorm.mag() + " @ " + angleTo(otherNorm));
    //System.out.println("otherParallel " + otherParallel.mag() + " @ " + angleTo(otherParallel));
    //System.out.println("otherPerp " + otherPerp.mag() + " @ " + angleTo(otherPerp));

    return this.plus(otherParallel).plus(otherPerp.div(gamma()))
        .div(1 + this.dot(other) / (c * c));
  }

  public float angleTo(Velocity other) {
    return (float) Math.acos(this.dot(other) / this.mag() / other.mag());
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

  public float mag() {
    return (float) Math.sqrt(vx * vx + vy * vy);
  }

  public Velocity norm() {
    float m = mag();
    if (m == 0) return Velocity.I;
    return this.times(1 / m);
  }

  public float dot(Velocity other) {
    return this.vx * other.vx + this.vy * other.vy;
  }

  public Velocity plus(Velocity other) {
    return new Velocity(this.vx + other.vx, this.vy + other.vy);
  }

  public Velocity minus(Velocity other) {
    return new Velocity(this.vx - other.vx, this.vy - other.vy);
  }

  @Override
  public String toString() {
    return "<" + vx + ", " + vy + ">";
  }
}

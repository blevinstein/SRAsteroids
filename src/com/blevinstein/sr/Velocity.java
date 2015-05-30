package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

public class Velocity {
  public static final Velocity ZERO = new Velocity(0, 0);
  public static final Velocity I = new Velocity(1, 0);
  public static final Velocity J = new Velocity(0, 1);

  private double vx;
  private double vy;

  public double x() { return vx; }
  public double y() { return vy; }

  public Velocity(double vx, double vy) {
    if (Double.isNaN(vx) || Double.isNaN(vy)) {
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

  public Velocity checked(double max) {
    double b = beta();
    return b > max ? this.times(max / b) : this;
  }

  public Event over(double t) {
    return new Event(vx * t, vy * t, t);
  }

  public Velocity times(double k) {
    return new Velocity(vx * k, vy * k);
  }

  public Velocity times(double kx, double ky) {
    return new Velocity(kx * vx, ky * vy);
  }

  public Velocity div(double k) {
    return new Velocity(vx / k, vy / k);
  }

  // BROKEN
  // http://en.wikipedia.org/wiki/Velocity-addition_formula#Special_theory_of_relativity
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

  public double angleTo(Velocity other) {
    return Math.acos(this.dot(other) / this.mag() / other.mag());
  }

  public double gamma() {
    return (1 / Math.sqrt(1 - beta_sq()));
  }

  public double beta() {
    return Math.sqrt(beta_sq());
  }

  public double beta_sq() {
    double bx = vx / c;
    double by = vy / c;
    return bx * bx + by * by;
  }

  public double mag() {
    return Math.sqrt(vx * vx + vy * vy);
  }

  public Velocity norm() {
    double m = mag();
    if (m == 0) return Velocity.I;
    return this.times(1 / m);
  }

  public double dot(Velocity other) {
    return this.vx * other.vx + this.vy * other.vy;
  }

  public Velocity plus(Velocity other) {
    return new Velocity(this.vx + other.vx, this.vy + other.vy);
  }

  public Velocity minus(Velocity other) {
    return new Velocity(this.vx - other.vx, this.vy - other.vy);
  }

  /**
   * @return a random Velocity with magnitude of 1
   */
  public static Velocity randomUnit() {
    double angle = Math.random() * 2 * Math.PI;
    return new Velocity(Math.cos(angle), Math.sin(angle));
  }

  @Override
  public String toString() {
    return "<" + vx + ", " + vy + ">";
  }
}

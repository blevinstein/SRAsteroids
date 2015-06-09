package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import java.util.Objects;

// TODO: calculate Thomas precession, use to correct positions after applying multiple lorentz
//   boosts
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
      throw new IllegalArgumentException("Component isNaN.");
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

  // http://en.wikipedia.org/wiki/Velocity-addition_formula
  // w = v + u =
  // 1 / (1 + v dot u / c^2) * (v + (1 / g) u + (1 / c^2) (g / (1 + g)) (v dot u) v)
  // where g = v.gamma()
  public Velocity relativePlus(Velocity other) {
    double g = this.gamma();
    double dotProduct = this.dot(other);
    return this.times(1 + 1 / (c * c) * g / (1 + g) * dotProduct)
      .plus(other.times(1 / g))
      .times(1 / (1 + dotProduct / (c * c)));
  }

  public Velocity relativeMinus(Velocity other) {
    return this.relativePlus(other.times(-1));
  }

  public double angleTo(Velocity other) {
    return Math.acos(this.dot(other) / this.mag() / other.mag());
  }

  public double angle() {
    return Math.atan2(vy, vx);
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

  public static Velocity unit(double angle) {
    return new Velocity(Math.cos(angle), Math.sin(angle));
  }

  /**
   * @return a random Velocity with magnitude of 1
   */
  public static Velocity randomUnit() {
    double angle = Math.random() * 2 * Math.PI;
    return unit(angle);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Velocity) {
      Velocity other = (Velocity) obj;
      return this.vx == other.vx
        && this.vy == other.vy;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "<" + vx + ", " + vy + ">";
  }

  @Override
  public int hashCode() {
    return Objects.hash(vx, vy);
  }
}

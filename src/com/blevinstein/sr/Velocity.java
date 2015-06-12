package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;
import static com.blevinstein.util.Trig.atanh;

import java.util.Objects;

/**
 * Represents a Velocity vector, (vx, vy) = (dx/dt, dy/dt)
 *
 * TODO: calculate Thomas precession, return as AffineTransform
 */
public class Velocity {
  public static final Velocity ZERO = new Velocity(0, 0); // zero velocity
  public static final Velocity I = new Velocity(1, 0); // unit velocity in +x direction
  public static final Velocity J = new Velocity(0, 1); // unit velocity in +y direction

  private final double vx;
  private final double vy;

  public double x() { return vx; }
  public double y() { return vy; }

  public Velocity(double vx, double vy) {
    if (Double.isNaN(vx) || Double.isNaN(vy)) {
      throw new IllegalArgumentException("Component isNaN.");
    }
    this.vx = vx;
    this.vy = vy;
  }

  /**
   * @return a Velocity perpendicular to this
   */
  public Velocity perp() {
    return new Velocity(vy, -vx);
  }

  /**
   * @return a valid Velocity
   */
  public Velocity checked() {
    return checked(1);
  }

  /**
   * @return this velocity, but limited to magnitude {@param max} * c, scaled down if necessary
   */
  public Velocity checked(double max) {
    double b = beta();
    return b > max ? this.times(max / b) : this;
  }

  /**
   * @return the spacetime interval covered by this over t units of time
   */
  public Event over(double t) {
    return new Event(vx * t, vy * t, t);
  }

  /**
   * @return result of multiplying each component of this velocity by k.
   *
   * NOTE: Does not prevent magnitude of velocity from exceeding c.
   * TODO: add relativeTimes, scale rapidity to avoid invalid values
   */
  public Velocity times(double k) {
    return new Velocity(vx * k, vy * k);
  }

  /**
   * @return result of multiplying each component i of this velocity by ki
   */
  public Velocity times(double kx, double ky) {
    return new Velocity(kx * vx, ky * vy);
  }

  /**
   * @return result of dividing this velocity by k
   */
  public Velocity div(double k) {
    return new Velocity(vx / k, vy / k);
  }

  /**
   * Performs relativistic velocity addition.
   *
   * http://en.wikipedia.org/wiki/Velocity-addition_formula
   * w = v + u =
   * 1 / (1 + v dot u / c^2) * (v + (1 / g) u + (1 / c^2) (g / (1 + g)) (v dot u) v)
   * where g = v.gamma()
   */
  public Velocity relativePlus(Velocity other) {
    double g = this.gamma();
    double dotProduct = this.dot(other);
    return this.times(1 + 1 / (c * c) * g / (1 + g) * dotProduct)
      .plus(other.times(1 / g))
      .times(1 / (1 + dotProduct / (c * c)));
  }

  /**
   * Performs relativistic velocity subtraction.
   */
  public Velocity relativeMinus(Velocity other) {
    return this.relativePlus(other.times(-1));
  }

  /**
   * Computes the shortest angle between two velocities, as if each was a vector from the origin.
   *
   * NOTE: uses dot product identity:  x dot y = |x| |y| cos(theta)
   */
  public double angleTo(Velocity other) {
    return Math.acos(this.dot(other) / this.mag() / other.mag());
  }

  /**
   * @return the angle, in radians, between velocity +x and this in the CCW direction
   */
  public double angle() {
    return Math.atan2(vy, vx);
  }

  /**
   * Calculates gamma at this velocity.
   *
   * gamma = 1 / sqrt(1 - |v|^2/c^2) = 1 / sqrt(1 - |b|^2) where b = v/c
   */
  public double gamma() {
    double bs = beta_sq();
    if (bs > 1) { throw new IllegalArgumentException("beta > 1"); }
    return (1 / Math.sqrt(1 - bs));
  }

  /**
   * Calculates magnitude of beta at this velocity.
   *
   * |b| = |v|/c = |v/c|
   */
  public double beta() {
    return Math.sqrt(beta_sq());
  }

  /**
   * Calculates magnitude squared of beta at this velocity.
   * |b|^2 = |v|^2/c^2 = |v/c|^2
   */
  public double beta_sq() {
    double bx = vx / c;
    double by = vy / c;
    return bx * bx + by * by;
  }

  /**
   * Calculates the magnitude of this.
   */
  public double mag() {
    return Math.sqrt(vx * vx + vy * vy);
  }

  /**
   * @return a Velocity in the same direction as this, with magnitude 1
   */
  public Velocity norm() {
    double m = mag();
    if (m == 0) return Velocity.I;
    return this.times(1 / m);
  }

  /**
   * Calculates the dot product of two velocities interpretted as vectors.
   */
  public double dot(Velocity other) {
    return this.vx * other.vx + this.vy * other.vy;
  }

  /**
   * Adds two velocities together, using component-wise addition (no relativistic effects).
   */
  public Velocity plus(Velocity other) {
    return new Velocity(this.vx + other.vx, this.vy + other.vy);
  }

  /**
   * Subtracts two velocities, using component-wise subtraction (no relativistics effects).
   */
  public Velocity minus(Velocity other) {
    return new Velocity(this.vx - other.vx, this.vy - other.vy);
  }

  /**
   * @return a unit velocity (magnitude = 1) with angle from +x velocity of {@param angle}
   */
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

  /**
   * @return the rapidity corresponding to the magnitude of this velocity
   * r / c = arctanh(|v| / c)
   */
  public double rapidity() {
    return atanh(beta()) * c;
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

package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import java.util.Objects;

/**
 * Represents a spacetime interval, represented as a vector in 3-space (x, y, t)
 *
 * Can be used to represent a point in a particular reference frame.
 */
public class Event {
  public static final Event ORIGIN = new Event(0, 0, 0);

  private final double _x;
  private final double _y;
  private final double _t;

  public double x() { return _x; }
  public double y() { return _y; }
  public double t() { return _t; }
  
  public Event(double x, double y, double t) {
    this._x = x;
    this._y = y;
    this._t = t;
  }

  /**
   * Adds together two spacetime intervals.
   */
  public Event plus(Event other) {
    return new Event(this._x + other._x,
        this._y + other._y,
        this._t + other._t);
  }

  /**
   * @return an Event offset from 'this' by 'interval' in the frame with 'relativeVelocity'
   */
  public Event relativePlus(Event interval, Velocity relativeVelocity) {
    return this.plus(SR.lorentz(interval, relativeVelocity.times(-1)));
  }

  /**
   * @return 'this' as seen by 'other' event in stationary reference frame
   */
  public Event minus(Event other) {
    return new Event(this._x - other._x,
        this._y - other._y,
        this._t - other._t);
  }

  /**
   * @return this spacetime interval multiplied by k.
   */
  public Event times(double k) {
    return new Event(this._x * k,
        this._y * k,
        this._t * k);
  }

  /**
   * @return this spacetime interval with dt added to its t coordinate
   */
  public Event advance(double dt) {
    return advance(0, 0, dt);
  }
  /**
   * @return this spacetime interval with (dx, dy, dt) added
   */
  public Event advance(double dx, double dy, double dt) {
    return new Event(_x + dx,
        _y + dy,
        _t + dt);
  }

  /**
   * @return length of this spacetime interval
   */
  public double interval() {
    return Math.sqrt(Math.abs(interval_sq()));
  }
  public double interval_sq() {
    return (c * _t) * (c * _t) - dist_sq();
  }

  /**
   * @return the length of the distance in space covered by this spacetime interval
   */
  public double dist() {
    return Math.sqrt(dist_sq());
  }
  /**
   * @return the square of the distance in space covered by this spacetime interval
   */
  public double dist_sq() {
    return _x * _x + _y * _y;
  }

  /**
   * @return whether this spacetime interval is spacelike, x^2 + y^2 > (ct)^2
   */
  public boolean isSpaceLike() {
    return interval_sq() < 0;
  }

  /**
   * @return whether this spacetime interval is lightlike, (ct)^2 ~ x^2 + y^2
   */
  public boolean isLightlike(double tolerance) {
    return Math.abs(interval_sq()) < tolerance;
  }

  /**
   * @return whether this spacetime interval is timelike, (ct)^2 > x^2 + y^2
   */
  public boolean isTimeLike() {
    return interval_sq() > 0;
  }

  /**
   * Calculates the Velocity of a direct path along this spacetime interval.
   */
  public Velocity toVelocity() {
    return new Velocity(_x / _t, _y / _t);
  }

  public Velocity toUnitVelocity() {
    return new Velocity(_x, _y).norm();
  }

  /**
   * Calculates the time elapsed for an object moving along this spacetime interval.
   */
  public double timeElapsed() {
    return _t / this.toVelocity().gamma();
  }

  /**
   * Implementation of equals() with additional tolerance argument
   */
  public boolean equals(Event other, double tolerance) {
    return Math.abs(this._x - other._x) < tolerance
        && Math.abs(this._y - other._y) < tolerance
        && Math.abs(this._t - other._t) < tolerance;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Event) {
      Event other = (Event) obj;
      return this._x == other._x &&
        this._y == other._y &&
        this._t == other._t;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "(" + this._x + ", " + this._y + ", " + this._t + ")";
  }

  @Override
  public int hashCode() {
    return Objects.hash(_x, _y, _t);
  }
}

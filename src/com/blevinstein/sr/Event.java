package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import java.util.Objects;

public class Event {
  public static final Event ORIGIN = new Event(0, 0, 0);

  private final double _x;
  private final double _y;
  private final double _t;

  public double x() { return _x; }
  public double y() { return _y; }
  public double t() { return _t; }
  
  public Event(double x, double y, double t) {
    if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(t)) {
      throw new IllegalArgumentException("Component isNaN.");
    }
    this._x = x;
    this._y = y;
    this._t = t;
  }

  /**
   * Returns a new Event with its t coordinate changed.
   */
  public Event withT(double t) {
    return new Event(_x, _y, t);
  }
  
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

  public Event times(double k) {
    return new Event(this._x * k,
        this._y * k,
        this._t * k);
  }

  public Event advance(double dt) {
    return advance(0, 0, dt);
  }
  public Event advance(double dx, double dy, double dt) {
    return new Event(_x + dx,
        _y + dy,
        _t + dt);
  }

  /**
   * @return length of the spacetime interval between this event and the observer/origin
   */
  public double interval() {
    return Math.sqrt(Math.abs(interval_sq()));
  }
  public double interval_sq() {
    return (c * _t) * (c * _t) - dist_sq();
  }

  /**
   * @return the length of the distance between this event and the observer
   */
  public double dist() {
    return Math.sqrt(dist_sq());
  }
  public double dist_sq() {
    return _x * _x + _y * _y;
  }

  /**
   * @return whether the spacetime interval is spacelike, x^2 + y^2 > (ct)^2
   */
  public boolean isSpaceLike() {
    return interval_sq() < 0;
  }

  /**
   * @return whether the spacetime interval is timelike, (ct)^2 > x^2 + y^2
   */
  public boolean isTimeLike() {
    return interval_sq() > 0;
  }

  public Velocity toVelocity() {
    return new Velocity(_x / _t, _y / _t);
  }

  public double timeElapsed() {
    return this.toVelocity().gamma() * _t;
  }

  public boolean equals(Event other, double tol) {
    return Math.abs(this._x - other._x) < tol
        && Math.abs(this._y - other._y) < tol
        && Math.abs(this._t - other._t) < tol;
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

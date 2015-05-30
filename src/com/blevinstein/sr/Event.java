package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

public class Event {
  private double _x;
  private double _y;
  private double _t;

  public double x() { return _x; }
  public double y() { return _y; }
  public double t() { return _t; }
  
  public Event(double x, double y, double t) {
    this._x = x;
    this._y = y;
    this._t = t;
  }
  
  public Event plus(Event other) {
    return new Event(this._x + other._x,
        this._y + other._y,
        this._t + other._t);
  }

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

  // @return 'this' as seen by 'other' event in stationary reference frame
  public Event relativeTo(Event other) {
    return new Event(this._x - other._x,
        this._y - other._y,
        this._t - other._t);
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
   * @return length squared of the spacetime interval between this event and the observer/origin
   */
  public double interval_sq() {
    return (c * _t) * (c * _t)
        - _x * _x - _y * _y;
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
}

package com.blevinstein.sr;

public class Event {
  private float _x;
  private float _y;
  private float _t;

  public float x() { return _x; }
  public float y() { return _y; }
  public float t() { return _t; }
  
  public Event(float x, float y, float t) {
    this._x = x;
    this._y = y;
    this._t = t;
  }
  
  public Event plus(Event other) {
    return new Event(this._x + other._x,
        this._y + other._y,
        this._t + other._t);
  }

  // @return 'this' as seen by 'other' event in stationary reference frame
  public Event relativeTo(Event other) {
    return new Event(this._x - other._x,
        this._y - other._y,
        this._t - other._t);
  }

  public Event advance(float dt) {
    return advance(0, 0, dt);
  }
  public Event advance(float dx, float dy, float dt) {
    return new Event(this._x + dx,
        this._y + dy,
        this._t + dt);
  }

  @Override
  public String toString() {
    return "(" + this._x + ", " + this._y + ", " + this._t + ")";
  }
}

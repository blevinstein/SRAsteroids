package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

/**
 * Represents a circular orbit around another timeline.
 *
 * k = 2pi / period
 * a = initial angle
 *
 * x = cx + r cos(k t + a)
 * y = cy + r sin(k t + a)
 *
 * dx/dt = dcx/dt - k r sin(k t)
 * dy/dt = dcy/dt + k r cos(k t)
 */
public class CircularTimeline extends Timeline {
  private Timeline _center;
  private double _radius;
  private double _angle;
  private double _k;

  public CircularTimeline(Timeline center, double radius, double velocity, double angle) {
    _center = center;
    _radius = radius;
    _k = velocity / _radius;
    _angle = angle;
  }

  public Event at(double t) {
    double properTime = _center.timeElapsed(0, t);
    Event offset = new Event(_radius * Math.cos(_k * properTime + _angle),
        _radius * Math.sin(_k * properTime + _angle),
        0);
    return _center.at(t).plus(offset);
  }

  public Velocity velocityAt(double t) {
    double properTime = _center.timeElapsed(0, t);
    Velocity relativeVelocity = new Velocity(_radius * _k * -Math.sin(_k * properTime + _angle),
        _radius * _k * Math.cos(_k * properTime + _angle));
    return _center.velocityAt(t).relativePlus(relativeVelocity);
  }

  public double timeElapsed(double tStart, double tEnd) {
    double gamma = new Velocity(_radius * _k, 0).gamma();
    return _center.timeElapsed(tStart, tEnd) * gamma;
  }

  @Override
  public String toString() {
    return String.format("CircularTimeline around(%s) r=%f k=%f",
        _center, _radius, _k);
  }
}

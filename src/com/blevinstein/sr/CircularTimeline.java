package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

/**
 * Represents a circular orbit around another timeline.
 * TODO: implement EllipticalTimeline
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
  private double _period;
  private double _angle;

  private double k() { return 2 * Math.PI / _period; }

  public CircularTimeline(Timeline center, double radius, double velocity, double angle) {
    _center = center;
    _radius = radius;
    _period = 2 * Math.PI * _radius / velocity;
    _angle = angle;
  }

  public Event at(double t) {
    double k = k();
    double properTime = _center.timeElapsed(0, t);
    Event offset = new Event(_radius * Math.cos(k * properTime + _angle),
        _radius * Math.sin(k * properTime + _angle),
        0);
    return _center.at(t).plus(offset);
  }

  public Velocity velocityAt(double t) {
    double k = k();
    double properTime = _center.timeElapsed(0, t);
    Velocity relativeVelocity = new Velocity(_radius * k * -Math.sin(k * properTime + _angle),
        _radius * k * Math.cos(k * properTime + _angle));
    return _center.velocityAt(t).relativePlus(relativeVelocity);
  }

  public double timeElapsed(double tStart, double tEnd) {
    double k = k();
    double gamma = new Velocity(_radius * k, 0).gamma();
    return _center.timeElapsed(tStart, tEnd) * gamma;
  }

  @Override
  public String toString() {
    return String.format("CircularTimeline around(%s) r=%f period=%f",
        _center, _radius, _period);
  }
}

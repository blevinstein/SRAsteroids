package com.blevinstein.geom;

/**
 * Defines a circular Region
 */
public class CircleRegion implements Region {
  private final Point _center;
  private final double _radius;

  public CircleRegion(Point center, double radius) {
    _center = center;
    _radius = radius;
  }

  public boolean contains(Point p) {
    return p.minus(_center).mag() <= _radius;
  }

  public Point boundary(double t) {
    double angle = 2 * Math.PI * t;
    return _center.plus(new Point(Math.cos(angle) * _radius, Math.sin(angle) * _radius));
  }
}

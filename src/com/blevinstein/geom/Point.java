package com.blevinstein.geom;

/**
 * Simple container object for a point in 2D space
 */
public class Point {
  private final double _x;
  private final double _y;

  public Point(double x, double y) {
    _x = x;
    _y = y;
  }

  public double x() { return _x; }
  public double y() { return _y; }

  public double mag() { return Math.sqrt(_x * _x + _y * _y); }

  public Point plus(Point other) { return new Point(_x + other._x, _y + other._y); }
  public Point minus(Point other) { return new Point(_x - other._x, _y - other._y); }
  public Point times(double k) { return new Point(_x * k, _y * k); }
}

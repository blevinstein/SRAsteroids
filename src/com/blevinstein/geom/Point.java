package com.blevinstein.geom;

import static org.junit.Assert.fail;

import java.util.Objects;

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
  public Point perp() { return new Point(-_y, _x); }
  public Point norm() {
    double m = mag(); // don't call mag() twice
    if (m == 0) { return new Point(1, 0); } // avoid division by zero
    return this.times(1 / m);
  }

  public Point plus(Point other) { return new Point(_x + other._x, _y + other._y); }
  public Point minus(Point other) { return new Point(_x - other._x, _y - other._y); }
  public Point times(double k) { return new Point(_x * k, _y * k); }
  public double dot(Point other) { return _x * other._x + _y * other._y; }

  public boolean equals(Point other, double tol) {
    return Math.abs(_x - other._x) < tol && Math.abs(_y - other._y) < tol;
  }

  @Override
  public String toString() {
    return String.format("(%f, %f)", _x, _y);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Point) {
      Point other = (Point) o;
      return _x == other._x && _y == other._y;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_x, _y);
  }

  // Testing methods

  public static void assertEquals(Point a, Point b, double tol) {
    assertEquals("", a, b, tol);
  }
  public static void assertEquals(String message, Point a, Point b, double tol) {
    if (!a.equals(b, tol)) {
      fail(String.format("%s != %s. %s", a, b, message));
    }
  }
}

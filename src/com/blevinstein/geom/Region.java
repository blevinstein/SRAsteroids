package com.blevinstein.geom;

/**
 * Defines a region in 2D space
 */
public interface Region {
  boolean contains(Point p);
  /**
   * Describes the boundary of the region, given t between 0 and 1 (inclusive)
   */
  Point boundary(double t);

  static Region and(Region a, Region b) {
    return new IntersectRegion(a, b);
  }

  static Region or(Region a, Region b) {
    // a OR b = NOT NOT a OR b = NOT (NOT a AND NOT b)
    return not(and(not(a), not(b)));
  }

  static Region not(Region other) {
    return new InvertRegion(other);
  }
}

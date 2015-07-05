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

  static Region not(Region other) {
    // TODO: implement NegativeRegion
    throw new UnsupportedOperationException();
  }
}

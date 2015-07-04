package com.blevinstein.geom;

/**
 * Defines a region in 2D space
 *
 * TODO: CircleRegion
 * TODO: LineRegion
 */
public interface Region {
  boolean contains(Point p);
  /**
   * Describes the boundary of the region, given t between 0 and 1 (inclusive)
   */
  Point boundary(double t);

  default Region intersect(Region other) {
    // TODO: implement IntersectRegion
    throw new UnsupportedOperationException();
  }

  default Region negate() {
    // TODO: implement NegativeRegion
    throw new UnsupportedOperationException();
  }
}

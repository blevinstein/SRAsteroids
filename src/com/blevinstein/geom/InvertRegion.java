package com.blevinstein.geom;

public class InvertRegion implements Region {
  private final Region region;

  public InvertRegion(Region region) {
    this.region = region;
  }

  // inverted
  public boolean contains(Point p) {
    return !region.contains(p);
  }

  // unchanged
  public Point boundary(double t) {
    return region.boundary(t);
  }
}

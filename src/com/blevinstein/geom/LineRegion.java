package com.blevinstein.geom;

/**
 * Defines a region on one side of a line
 */
public class LineRegion {
  private final Point _line;
  private final Point _containsDir;
  private final Point _lineDir;

  /**
   * @param line a Point on the line
   * @param containsDir a Point giving a direction perpendicular to the line, pointing towards
   *   the contained region
   */
  public LineRegion(Point line, Point containsDir) {
    _line = line;
    _containsDir = containsDir.norm();
    _lineDir = _containsDir.perp();
  }

  public boolean contains(Point p) {
    return p.minus(_line).dot(_containsDir) > 0;
  }

  public Point boundary(double t) {
    double angle = (2 * t - 1) * (Math.PI / 2) * (1 - 1E-10); // prevent Infinity
    return _line.plus(_lineDir.times(Math.tan(angle)));
  }
}

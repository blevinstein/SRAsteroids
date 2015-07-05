package com.blevinstein.geom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * NOTE: intersected region may have UGLY discontinuities
 */
public class IntersectRegion implements Region {
  private final Region a, b;
  private final List<Section> aBoundary, bBoundary;

  public IntersectRegion(Region a, Region b) {
    this.a = a;
    this.b = b;
  
    aBoundary = getBoundaryContained(a, b);
    bBoundary = getBoundaryContained(b, a);
  }

  public boolean contains(Point p) {
    return a.contains(p) && b.contains(p);
  }

  public Point boundary(double t) {
    if (t < 0.5) {
      // boundary pieces from a
      int i = (int)(aBoundary.size() * t / 0.5);
      double tPrime = t % (0.5 / aBoundary.size());
      Section s = aBoundary.get(i);
      return a.boundary(s.t0() * (1 - tPrime) + s.t1() * tPrime);
    } else {
      // boundary pieces from b
      int i = t == 1.0
        ? bBoundary.size() - 1
        : (int)(bBoundary.size() * (t - 0.5) / 0.5);
      double tPrime = t % (0.5 / bBoundary.size());
      Section s = bBoundary.get(i);
      return a.boundary(s.t0() * (1 - tPrime) + s.t1() * tPrime);
    }
  }

  /**
   * @return the parts of a's boundary that are contained in b
   *
   * NOTE: simple sampling will fail if the intersected boundaries are too complex
   */
  private static final int getBoundaryContained_N = 10;
  private List<Section> getBoundaryContained(Region a, Region b) {
    // sample N points on boundary
    double sampleDist = 1.0 / getBoundaryContained_N;
    Point[] points = new Point[getBoundaryContained_N];
    for (int i = 0; i < getBoundaryContained_N; i++) {
      points[i] = a.boundary(i * sampleDist);
    }
    // find sections
    List<Section> containedSections = new ArrayList<>();
    double startSection = 0;
    boolean containedFlag = b.contains(a.boundary(0));
    for (int i = 0; i < getBoundaryContained_N - 1; i++) {
      // only consider sections that cross a boundary
      if (b.contains(a.boundary(i * sampleDist))
          == b.contains(a.boundary((i + 1) * sampleDist))) {
            continue;
      }
      // find the point where it crosses
      double crossover = bisectionMethod((Double t) -> b.contains(a.boundary(t)),
          i * sampleDist, (i + 1) * sampleDist);
      if (containedFlag) {
        // add new section and reset
        containedSections.add(new Section(startSection, crossover));
        containedFlag = false;
      } else {
        // start new section
        startSection = crossover;
        containedFlag = true;
      }
    }
    // add last section if needed
    if (containedFlag && startSection < 1) {
      containedSections.add(new Section(startSection, 1));
    }

    return containedSections;
  }

  private static final double bisectionMethod_tol = 0.001;
  private static double bisectionMethod(Function<Double, Boolean> f, double tLow, double tHigh) {
    // precondition for bisection method
    if (f.apply(tLow) == f.apply(tHigh)) { throw new IllegalArgumentException(); }
    
    while (tHigh - tLow > bisectionMethod_tol) {
      double tMid = (tLow + tHigh) / 2;
      if (f.apply(tMid) == f.apply(tLow)) {
        tLow = tMid;
      } else {
        tHigh = tMid;
      }
    }

    // always return the contained value
    return f.apply(tLow) ? tLow : tHigh;
  }

  class Section {
    private final double _t0, _t1;

    public Section(double t0, double t1) {
      _t0 = t0;
      _t1 = t1;
    }

    public double t0() { return _t0; }
    public double t1() { return _t1; }
  }
}

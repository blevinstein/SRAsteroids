package com.blevinstein.geom;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class RegionTest {
  @Test
  public void vennDiagram() {
    Region a = new CircleRegion(new Point(-5, 0), 10);
    Region b = new CircleRegion(new Point(5, 0), 10);
    Point inA = new Point(-7, 0);
    Point inB = new Point(7, 0);
    Point inBoth = new Point(0, 0);
    Point inNone = new Point(0, 10);

    // basic sanity checking

    assertTrue(a.contains(inA));
    assertTrue(a.contains(inBoth));
    assertTrue(b.contains(inB));
    assertTrue(b.contains(inBoth));

    assertFalse(a.contains(inB));
    assertFalse(a.contains(inNone));
    assertFalse(b.contains(inA));
    assertFalse(b.contains(inNone));

    // NOT a

    Region notA = Region.not(a);
    assertTrue(notA.contains(inNone));
    assertFalse(notA.contains(inA));

    // a AND b

    Region aAndB = Region.and(a, b);

    assertTrue(aAndB.contains(inBoth));
    assertFalse(aAndB.contains(inA));
    assertFalse(aAndB.contains(inB));
    assertFalse(aAndB.contains(inNone));

    // a AND NOT b

    Region aAndNotB = Region.and(a, Region.not(b));

    assertTrue(aAndNotB.contains(inA));
    assertFalse(aAndNotB.contains(inB));
    assertFalse(aAndNotB.contains(inBoth));
    assertFalse(aAndNotB.contains(inNone));

    // a OR b

    Region aOrB = Region.or(a, b);

    assertTrue(aOrB.contains(inA));
    assertTrue(aOrB.contains(inB));
    assertTrue(aOrB.contains(inBoth));
    assertFalse(aOrB.contains(inNone));
  }

  @Test
  public void semicircle() {
    // circle around origin, radius = 10, where y > 0
    Region r = Region.and(
        new CircleRegion(new Point(0, 0), 10),
        new LineRegion(new Point(0, 0), new Point(0, 1)));

    assertTrue(r.contains(new Point(0, 9)));
    assertFalse(r.contains(new Point(0, 11)));
    assertFalse(r.contains(new Point(0, -1)));
  }
}

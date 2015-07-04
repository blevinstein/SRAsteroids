package com.blevinstein.geom;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CircleRegionTest {
  @Test
  public void simple() {
    CircleRegion region = new CircleRegion(new Point(10, 0), 5);

    assertEquals(true, region.contains(new Point(10, 0)));
    assertEquals(false, region.contains(new Point(0, 0)));
    assertEquals(true, region.contains(new Point(10, 5)));
    assertEquals(true, region.contains(new Point(15, 0)));
    assertEquals(false, region.contains(new Point(15.01, 0)));

    Point.assertEquals(new Point(15, 0), region.boundary(0), 0.001);
    Point.assertEquals(new Point(10, 5), region.boundary(0.25), 0.001);
    Point.assertEquals(new Point(5, 0), region.boundary(0.5), 0.001);
    Point.assertEquals(new Point(10, -5), region.boundary(0.75), 0.001);
  }
}

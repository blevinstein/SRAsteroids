package com.blevinstein.geom;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LineRegionTest {
  @Test
  public void diagonal() {
    LineRegion region = new LineRegion(new Point(100, 0), new Point(1, 1));

    assertEquals(true, region.contains(new Point(100, 1)));
    assertEquals(true, region.contains(new Point(101, 0)));
    assertEquals(false, region.contains(new Point(99, 0)));
    assertEquals(false, region.contains(new Point(100, -1)));

    for (double i = 0.0; i <= 1.0; i += 0.1) {
      Point onBoundary = region.boundary(i);
      // y = - x + 100
      // y + x = 100
      assertEquals(100.0, onBoundary.x() + onBoundary.y(), 0.01);
    }
  }
}

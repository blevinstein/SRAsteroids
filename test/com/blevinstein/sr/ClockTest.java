package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ClockTest {
  @Test
  public void stationary() {
    Clock c = new Clock(new Event(0, 0, 0));
    c.move(new Event(0, 0, 10));
    c.move(new Event(0, 0, 20));
    assertEquals(c.time(), 20, 0.001);
  }

  @Test
  public void twinParadox() {
    Clock a = new Clock(new Event(0, 0, 0));
    Clock b = new Clock(new Event(0, 0, 0));
    double gamma = 1 / Math.sqrt(1 - 0.5 * 0.5);
    a.move(new Event(5 * c, 0, 10));
    a.move(new Event(0, 0, 20));
    b.move(new Event(0, 0, 10));
    b.move(new Event(0, 0, 20));
    assertEquals(20 / gamma, a.time(), 0.001);
    assertEquals(20, b.time(), 0.001);
  }
}


package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SRTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void lorentz_stationary() {
    assertEquals(new Event(1, 2, 3), SR.lorentz(new Event(1, 2, 3), Velocity.ZERO));
    assertEquals(new Event(0, 0, 0), SR.lorentz(new Event(0, 0, 0), Velocity.ZERO));
  }

  @Test
  public void lorentz_lengthContraction() {
    Velocity v = new Velocity(5, 0);
    assertEquals(3 * v.gamma(), SR.lorentz(new Event(3, 0, 0), v).x(), 0.001);
    assertEquals(-3 * v.gamma(), SR.lorentz(new Event(-3, 0, 0), v).x(), 0.001);
  }

  @Test
  public void lorentz_timeDilation() {
    Velocity v = new Velocity(4, 0);
    assertEquals(7 * v.gamma(), SR.lorentz(new Event(0, 0, 7), v).t(), 0.001);
    assertEquals(-7 * v.gamma(), SR.lorentz(new Event(0, 0, -7), v).t(), 0.001);
  }
}

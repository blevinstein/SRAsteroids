package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SRTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  public static void assertEquals(Event a, Event b, double tol) {
    assertEquals("", a, b, tol);
  }
  public static void assertEquals(String message, Event a, Event b, double tol) {
    if (!a.equals(b, tol)) {
      Assert.fail(String.format("%s != %s. %s", a, b, message));
    }
  }

  @Test
  public void lorentz_stationary() {
    assertEquals(new Event(1, 2, 3), SR.lorentz(new Event(1, 2, 3), Velocity.ZERO), 0.001);
    assertEquals(new Event(0, 0, 0), SR.lorentz(new Event(0, 0, 0), Velocity.ZERO), 0.001);
  }

  @Test
  public void lorentz_lengthContraction() {
    Velocity v = new Velocity(c / 2, 0);
    Assert.assertEquals(3 * v.gamma(), SR.lorentz(new Event(3, 0, 0), v).x(), 0.001);
    Assert.assertEquals(-3 * v.gamma(), SR.lorentz(new Event(-3, 0, 0), v).x(), 0.001);
  }

  @Test
  public void lorentz_timeDilation() {
    Velocity v = new Velocity(c * 0.4, 0);
    Assert.assertEquals(7 * v.gamma(), SR.lorentz(new Event(0, 0, 7), v).t(), 0.001);
    Assert.assertEquals(-7 * v.gamma(), SR.lorentz(new Event(0, 0, -7), v).t(), 0.001);
  }

  @Test
  public void lorentz_reversible() {
    Velocity v = new Velocity(0.2 * c, 0.2 * c);
    Event e = new Event(1, 2, 3);
    assertEquals(e, SR.lorentz(SR.lorentz(e, v), v.times(-1)), 0.001);
  }
}

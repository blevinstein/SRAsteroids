package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VelocityTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void angleTo() {
    Velocity v1 = new Velocity(10, 20);
    Velocity v2 = new Velocity(-5, 25);

    assertEquals(Math.abs(v1.angle() - v2.angle()), v1.angleTo(v2), 0.01);
  }

  @Test
  public void relativePlus() {
    assertEquals(new Velocity(40, 0),
        new Velocity(25, 0).relativePlus(new Velocity(25, 0)));
  }

  @Test
  //TODO: Fix. Either my math or my understanding of SR is incorrect.
  public void rapidity() {
    assertEquals(0.5493 * c, new Velocity(0.5 * c, 0).rapidity(), 0.001);
    assertEquals(1.0986 * c, new Velocity(0.8 * c, 0).rapidity(), 0.01);

    Velocity v = new Velocity(25, 0);
    assertEquals(v.rapidity() * 2, v.relativePlus(v).rapidity(), 0.001);
    // TODO: assertEquals(v.rapidity() * 2, v.relativeTimes(2).rapidity(), 0.001);
  }
}

package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VelocityTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void angleTo() {
    Velocity v1 = new Velocity(10, 20);
    Velocity v2 = new Velocity(-5, 50);

    Assert.assertEquals(Math.abs(v1.angle() - v2.angle()), v1.angleTo(v2), 0.01);
  }
}

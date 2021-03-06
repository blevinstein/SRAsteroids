package com.blevinstein.sr;

import static com.blevinstein.sr.SRTest.assertEquals;
import static org.junit.Assert.assertEquals;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;

import org.junit.Test;

public class EllipticalTimelineTest {
  /**
   * test case:
   * major axis a = 5
   * minor axis b = 4
   * focus f = 3
   * eccentricity e = 3/5
   * radius R = 16/5
   * S = 4/sqrt(5)
   */
  @Test
  public void simpleEllipse() {
    EllipticalTimeline et = new EllipticalTimeline(
        0 /* angle_init */,
        0 /* angle_perih */,
        new StaticTimeline(0, 0),
        false /* cw */,
        0.6 /* eccentricity */,
        1 /* gravity */,
        5 /* major_axis */,
        1 /* t0 */);

    double t = et.timeAt(0); // perihelion
    assertEquals(1, t, 0.01);
    assertEquals(new Event(2, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, 2 / Math.sqrt(5)), et.velocityAt(t), 0.01);
   
    t = et.timeAt(Math.PI/2);
    assertEquals(1 + 4.999, t, 0.01);
    assertEquals(new Event(0, 16.0/5, t), et.at(t), 0.01);

    t = et.timeAt(Math.PI); // aphelion
    assertEquals(new Event(-8, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, -1 / (2 * Math.sqrt(5))), et.velocityAt(t), 0.01);

    t = et.timeAt(3*Math.PI/2);
    assertEquals(new Event(0, -16.0/5, t), et.at(t), 0.01);
  }

  @Test
  public void simpleEllipse_oppositeDirection() {
    EllipticalTimeline et = new EllipticalTimeline(
        0 /* angle_init */,
        0 /* angle_perih */,
        new StaticTimeline(0, 0),
        true /* cw */,
        0.6 /* eccentricity */,
        1 /* gravity */,
        5 /* major_axis */,
        1 /* t0 */);

    double t = et.timeAt(0); // perihelion
    assertEquals(1, t, 0.01);
    assertEquals(new Event(2, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, -2 / Math.sqrt(5)), et.velocityAt(t), 0.01);

    t = et.timeAt(3 * Math.PI/2);
    assertEquals(new Event(0, -16.0/5, t), et.at(t), 0.01);

    t = et.timeAt(Math.PI); // aphelion
    assertEquals(new Event(-8, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, 1 / (2 * Math.sqrt(5))), et.velocityAt(t), 0.01);

    t = et.timeAt(Math.PI/2);
    assertEquals(new Event(0, 16.0/5, t), et.at(t), 0.01);
  }

  @Test
  public void simpleEllipse_create() {
    EllipticalTimeline et = EllipticalTimeline.create(new StaticTimeline(0, 0), 1 /* gravity */,
        new Event(2, 0, 1), new Velocity(0, 2 / Math.sqrt(5)));

    double t = et.timeAt(0); // perihelion
    assertEquals(1, t, 0.01);
    assertEquals(new Event(2, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, 2 / Math.sqrt(5)), et.velocityAt(t), 0.01);

    t = et.timeAt(Math.PI/2);
    assertEquals(1 + 4.999, t, 0.01);
    assertEquals(new Event(0, 16.0/5, t), et.at(t), 0.01);

    t = et.timeAt(Math.PI); // aphelion
    assertEquals(new Event(-8, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, -1 / (2 * Math.sqrt(5))), et.velocityAt(t), 0.01);

    t = et.timeAt(3*Math.PI/2);
    assertEquals(new Event(0, -16.0/5, t), et.at(t), 0.01);
  }

  @Test
  public void simpleEllipse_createOpposite() {
    EllipticalTimeline et = EllipticalTimeline.create(new StaticTimeline(0, 0), 1 /* gravity */,
        new Event(2, 0, 1), new Velocity(0, -2 / Math.sqrt(5)));

    double t = et.timeAt(0); // perihelion
    assertEquals(1, t, 0.01);
    assertEquals(new Event(2, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, -2 / Math.sqrt(5)), et.velocityAt(t), 0.01);

    t = et.timeAt(3 * Math.PI/2);
    assertEquals(new Event(0, -16.0/5, t), et.at(t), 0.01);

    t = et.timeAt(Math.PI); // aphelion
    assertEquals(new Event(-8, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, 1 / (2 * Math.sqrt(5))), et.velocityAt(t), 0.01);

    t = et.timeAt(Math.PI/2);
    assertEquals(new Event(0, 16.0/5, t), et.at(t), 0.01);
  }

  @Test
  public void simpleEllipse_createAtAphelion() {
    EllipticalTimeline et = EllipticalTimeline.create(new StaticTimeline(0, 0), 1 /* gravity */,
        new Event(-8, 0, 1), new Velocity(0, -1 / (2 * Math.sqrt(5))));

    double t = et.timeAt(0); // perihelion
    assertEquals(new Event(2, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, 2 / Math.sqrt(5)), et.velocityAt(t), 0.01);

    t = et.timeAt(Math.PI/2);
    assertEquals(new Event(0, 16.0/5, t), et.at(t), 0.01);

    t = et.timeAt(Math.PI); // aphelion
    assertEquals(new Event(-8, 0, t), et.at(t), 0.01);
    assertEquals(new Velocity(0, -1 / (2 * Math.sqrt(5))), et.velocityAt(t), 0.01);

    t = et.timeAt(3*Math.PI/2);
    assertEquals(new Event(0, -16.0/5, t), et.at(t), 0.01);
  }

  @Test
  public void simpleEllipse_rotated() {
    EllipticalTimeline et = new EllipticalTimeline(
        0 /* angle_init */,
        Math.PI/2 /* angle_perih */,
        new StaticTimeline(0, 0),
        false /* cw */,
        0.6 /* eccentricity */,
        1 /* gravity */,
        5 /* major_axis */,
        0 /* t0 */);

    double t = et.timeAt(Math.PI/2); // perihelion
    assertEquals(4.999, t, 0.01);
    assertEquals(Math.PI/2, et.thetaAt(t), 0.01);
    assertEquals(new Event(0, 2, t), et.at(t), 0.01);
    assertEquals(new Velocity(-2 / Math.sqrt(5), 0), et.velocityAt(t), 0.01);

    t = et.timeAt(Math.PI);
    assertEquals(2 * 4.999, t, 0.01);
    assertEquals(Math.PI, et.thetaAt(t), 0.01);
    assertEquals(new Event(-16.0/5, 0, t), et.at(t), 0.01);

    t = et.timeAt(3*Math.PI/2); // aphelion
    assertEquals(new Event(0, -8, t), et.at(t), 0.01);
    assertEquals(new Velocity(1 / (2 * Math.sqrt(5)), 0), et.velocityAt(t), 0.01);

    t = et.timeAt(2*Math.PI);
    assertEquals(new Event(16.0/5, 0, t), et.at(t), 0.01);
  }
}

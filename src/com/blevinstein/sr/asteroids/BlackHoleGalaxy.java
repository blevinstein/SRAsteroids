package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.ConstantTimeline;
import com.blevinstein.sr.EllipticalTimeline;
import com.blevinstein.sr.StaticTimeline;
import com.blevinstein.sr.Event;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;

/**
 * Like MutableGalaxy, but tries to keep stars in orbit around central black hole by calculating
 *   new elliptical orbits on collision
 */
public class BlackHoleGalaxy extends MutableGalaxy {
  private static final StaticTimeline CENTER = new StaticTimeline(0, 0);

  private final double gravity;

  public BlackHoleGalaxy(double gravity) {
    this.gravity = gravity;
  }

  @Override
  public synchronized void create(StarDef newDef, Event start, Velocity init) {
    Timeline timeline;
    try {
      timeline = EllipticalTimeline.create(CENTER, gravity, start, init).limit(start, null);
      System.out.println("new ellipse");
    } catch (IllegalArgumentException | IllegalStateException e) {
      timeline = new ConstantTimeline(start, init).limit(start, null);
      System.out.println("fallback");
    }
    add(new Star(timeline, newDef));
  }
}

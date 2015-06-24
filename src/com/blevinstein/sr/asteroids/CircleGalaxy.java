package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.CircularTimeline;
import com.blevinstein.sr.Event;
import com.blevinstein.sr.StaticTimeline;
import com.blevinstein.sr.Velocity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a galaxy of stars rotating in circles around the center.
 * @param maxRadius the maximum radius of any orbiting star
 * @param density in stars per area
 * @param gravity of central black hole (units meter^3/second^2)
 */
public class CircleGalaxy extends MutableGalaxy {
  public CircleGalaxy(double maxRadius, double density, double gravity) {
    StaticTimeline blackHole = new StaticTimeline(0, 0);

    int numStars = (int)(Math.PI * maxRadius * maxRadius * density);
    for (int i = 0; i < numStars; i++) {
      // Create a new star
      double angle = Math.random() * 2 * Math.PI;
      double minRadius = gravity / (c * c); // so that velocity < c
      double dist = Math.sqrt(Math.random()) * (maxRadius - minRadius) + minRadius;
      double velocity = Math.sqrt(gravity  / dist) * (Math.random() < 0.5 ? 1 : -1);
      CircularTimeline starTimeline = new CircularTimeline(blackHole, dist, velocity, angle);
      add(new Star(
            starTimeline,
            Color.GRAY,
            10 /* radius */,
            10 /* twinklePeriod */));
    }
  }
}

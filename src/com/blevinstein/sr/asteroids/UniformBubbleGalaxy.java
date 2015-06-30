package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.ConstantTimeline;
import com.blevinstein.sr.Event;
import com.blevinstein.sr.Velocity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a galaxy limited to a 'bubble' in a sphere around origin and uniform density.
 */
public class UniformBubbleGalaxy extends MutableGalaxy {
  /**
   * @param radius of the bubble
   * @param density in stars per area
   */
  public UniformBubbleGalaxy(double radius, double density) {
    int numStars = (int) (Math.PI * radius * radius * density);
    for (int i = 0; i < numStars; i++) {
      // Create a new star
      double angle = Math.random() * 2 * Math.PI;
      double dist = Math.sqrt(Math.random()) * radius;
      Event starPosition = new Event(dist * Math.cos(angle), dist * Math.sin(angle), 0);
      Velocity starVelocity = Velocity.randomUnit().times(Math.random() * 0.999 * c);
      ConstantTimeline starTimeline = new ConstantTimeline(starPosition, starVelocity);
      add(new Star(starTimeline,
          new StarDef(Color.GRAY, 10 /* radius */, 10 /* twinklePeriod */)));
    }
  }
}


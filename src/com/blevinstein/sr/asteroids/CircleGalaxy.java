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
 * @param radius the maximum radius of any orbiting star
 * @param density in stars per area
 */
public class CircleGalaxy implements Galaxy {
  public List<Star> _stars;

  public CircleGalaxy(double radius, double density) {
    StaticTimeline blackHole = new StaticTimeline(0, 0);

    _stars = new ArrayList<>();
    int numStars = (int)(Math.PI * radius * radius * density);
    for (int i = 0; i < numStars; i++) {
      // Create a new star
      double angle = Math.random() * 2 * Math.PI;
      double dist = Math.sqrt(Math.random()) * radius;
      double velocity = Math.random() * 0.99 * c * (Math.random() < 0.5 ? 1 : -1);
      CircularTimeline starTimeline = new CircularTimeline(blackHole, dist, velocity, angle);
      _stars.add(new Star(
            starTimeline,
            Color.GRAY,
            10 /* radius */,
            10 /* twinklePeriod */));
    }
  }

  public List<Star> stars() {
    // return defensive copy
    return new ArrayList<>(_stars);
  }
}

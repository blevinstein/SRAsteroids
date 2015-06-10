package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Velocity;
import com.blevinstein.sr.ConstantTimeline;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a galaxy limited to a 'bubble' in a sphere around origin and uniform density.
 * @param radius of the bubble
 * @param density in stars per area
 * NOTE: All stars are white for now.
 */
public class UniformBubbleGalaxy implements Galaxy {
  public List<Star> _stars;

  public UniformBubbleGalaxy(double radius, double density) {
    _stars = new ArrayList<>();
    int numStars = (int) (Math.PI * radius * radius * density);
    for (int i = 0; i < numStars; i++) {
      // Create a new star
      double angle = Math.random() * 2 * Math.PI;
      double dist = Math.sqrt(Math.random()) * radius;
      Event starPosition = new Event(dist * Math.cos(angle), dist * Math.sin(angle), 0);
      Velocity starVelocity = Velocity.randomUnit().times(Math.random() * 0.9 * c);
      ConstantTimeline starTimeline = new ConstantTimeline(starPosition, starVelocity);
      _stars.add(new Star(starTimeline, Color.WHITE, 10));
    }
  }

  public List<Star> stars() {
    // return defensive copy
    return new ArrayList<>(_stars);
  }
}


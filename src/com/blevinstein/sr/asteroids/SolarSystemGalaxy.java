package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;
import static com.blevinstein.util.Prob.poisson;

import com.blevinstein.sr.CircularTimeline;
import com.blevinstein.sr.Event;
import com.blevinstein.sr.StaticTimeline;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a solar system, with moons orbiting planets orbiting the sun
 */
public class SolarSystemGalaxy implements Galaxy {
  public List<Star> _stars;

  public SolarSystemGalaxy(double maxRadius, double expPlanets, double expMoons,
      double gravity) {
    _stars = new ArrayList<>();
    
    StaticTimeline sun = new StaticTimeline(100, 0);
    _stars.add(new Star(sun, Color.YELLOW, 50 /* radius */, 100 /* twinklePeriod */));

    int numPlanets = poisson(expPlanets);
    for (int i = 0; i < numPlanets; i++) {
      CircularTimeline planet = randomOrbit(sun, maxRadius, gravity);
      _stars.add(new Star(planet, randomColor(), 10 /* radius */, 0 /* no twinkle */));
      
      int numMoons = poisson(expMoons);
      for (int j = 0; j < numMoons; j++) {
        CircularTimeline moon = randomOrbit(planet, maxRadius/numPlanets, gravity/numPlanets);
        _stars.add(new Star(moon, randomColor(), 5 /* radius */, 0 /* no twinkle */));
      }
    }
  }

  private CircularTimeline randomOrbit(Timeline center, double maxRadius, double gravity) {
    double angle = Math.random() * 2 * Math.PI;
    double minRadius = gravity / (c * c); // so that velocity < c
    double radius = Math.sqrt(Math.random()) * (maxRadius - minRadius) + minRadius;
    double velocity = Math.sqrt(gravity / radius);
    return new CircularTimeline(center, radius, velocity, angle);
  }

  private Color randomColor() {
    return Color.getHSBColor((float) Math.random(), (float) Math.random(), 0.8f);
  }

  public List<Star> stars() {
    // return defensive copy
    return new ArrayList<>(_stars);
  }
}

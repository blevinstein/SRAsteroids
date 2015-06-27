package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;
import static com.blevinstein.util.Prob.poisson;

import com.blevinstein.sr.EllipticalTimeline;
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
public class SolarSystemGalaxy extends MutableGalaxy {
  public SolarSystemGalaxy(double maxRadius, double expPlanets, double expMoons,
      double gravity) {
    StaticTimeline sun = new StaticTimeline(0, 0);
    add(new Star(sun, Color.YELLOW, 50 /* radius */, 100 /* twinklePeriod */));

    int numPlanets = poisson(expPlanets);
    for (int i = 0; i < numPlanets; i++) {
      EllipticalTimeline planet = randomOrbit(sun, maxRadius, gravity);
      add(new Star(planet, randomColor(), 10 /* radius */, 0 /* no twinkle */));
      
      int numMoons = poisson(expMoons);
      for (int j = 0; j < numMoons; j++) {
        EllipticalTimeline moon = randomOrbit(planet, maxRadius/numPlanets, gravity/numPlanets);
        add(new Star(moon, randomColor(), 5 /* radius */, 0 /* no twinkle */));
      }
    }
  }

  private EllipticalTimeline randomOrbit(Timeline center, double maxRadius, double gravity) {
    double angle = Math.random() * 2 * Math.PI;
    double perihelion = Math.random() * 2 * Math.PI;
    double eccentricity = Math.random() * EllipticalTimeline.MAX_ECCENTRICITY;
    double maxMajorAxis = maxRadius / (1 + eccentricity);
    double E = 2 / (1 - eccentricity) - 1;
    double minMajorAxis = gravity * E / (c * c);
    if (minMajorAxis > maxMajorAxis) {
      System.out.println("randomOrbit abort");
      return randomOrbit(center, maxRadius, gravity);
    }
    double majorAxis = Math.sqrt(Math.random()) * (maxMajorAxis - minMajorAxis) + minMajorAxis;
    return new EllipticalTimeline(angle, perihelion, center, false /* cw */, eccentricity,
        gravity, majorAxis, 0 /* t0 */);
  }

  private Color randomColor() {
    return Color.getHSBColor((float) Math.random(), (float) Math.random(), 0.8f);
  }
}

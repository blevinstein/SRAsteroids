package com.blevinstein.sr.asteroids;

import java.awt.Color;

/**
 * Container object for Star properties.
 */
public class StarDef {
  private final Color _color;
  private final double _radius;
  private final double _twinklePeriod; // rate of twinkling in Hz
  private final double _gravity;

  public StarDef(Color color, double radius, double twinklePeriod) {
    this(color, radius, twinklePeriod, 1);
  }
  public StarDef(Color color, double radius, double twinklePeriod, double gravity) {
    _color = color;
    _radius = radius;
    _twinklePeriod = twinklePeriod;
    _gravity = gravity;
  }

  public Color color() { return _color; }
  public double radius() { return _radius; }
  public double twinklePeriod() { return _twinklePeriod; }
  public double gravity() { return _gravity; }
}


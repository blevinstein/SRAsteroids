package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Timeline;

import java.awt.Color;

/**
 * Container object for a Star.
 */
public class Star {
  private final Timeline _timeline;
  private final Color _color;
  private final double _radius;
  private final double _twinklePeriod; // rate of twinkling in Hz

  public Star(Timeline timeline, Color color, double radius) {
    this(timeline, color, radius, 0);
  }
  public Star(Timeline timeline, Color color, double radius, double twinklePeriod) {
    _timeline = timeline;
    _color = color;
    _radius = radius;
    _twinklePeriod = twinklePeriod;
  }

  public Timeline timeline() { return _timeline; }
  public Color color() { return _color; }
  public double radius() { return _radius; }
  public double twinklePeriod() { return _twinklePeriod; }
}


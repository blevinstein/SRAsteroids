package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Timeline;

import java.awt.Color;

/**
 * Container object for a Star.
 */
// TODO: add period? pulsing/flashing of some sort?
public class Star {
  private final Timeline _timeline;
  private final Color _color;
  private final double _radius;

  public Star(Timeline timeline, Color color, double radius) {
    _timeline = timeline;
    _color = color;
    _radius = radius;
  }

  public Timeline timeline() { return _timeline; }
  public Color color() { return _color; }
  public double radius() { return _radius; }
}


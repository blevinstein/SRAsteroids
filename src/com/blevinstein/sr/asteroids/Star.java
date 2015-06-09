package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Timeline;

import java.awt.Color;

/**
 * Container object for a Star.
 */
// TODO: add size
// TODO: add period? pulsing/flashing of some sort?
public class Star {
  private Timeline _timeline;
  private Color _color;

  public Star(Timeline timeline, Color color) {
    _timeline = timeline;
    _color = color;
  }

  public Timeline timeline() { return _timeline; }
  public Color color() { return _color; }
}


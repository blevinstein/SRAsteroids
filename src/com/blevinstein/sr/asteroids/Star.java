package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Timeline;

import java.awt.Color;

/**
 * Container object for a Star
 *
 * Contains dynamic data about a Star that changes during simulation
 *
 * A static definition of the star's properties is contained in StarDef _def
 */
public class Star {
  private final StarDef _def;
  private Timeline _timeline;

  public Star(Timeline timeline, StarDef def) {
    _timeline = timeline;
    _def = def;
  }
  
  public Timeline timeline() { return _timeline; }
  public StarDef def() { return _def; }

  public Star setTimeline(Timeline timeline) {
    _timeline = timeline;
    return this;
  }

  // delegate to def
  public Color color() { return _def.color(); }
  public double radius() { return _def.radius(); }
  public double twinklePeriod() { return _def.twinklePeriod(); }
  public double gravity() { return _def.gravity(); }
}


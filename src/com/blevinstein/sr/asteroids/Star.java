package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Timeline;

import java.awt.Color;

/**
 * Container object for a Star.
 */
public class Star {
  private Timeline _timeline;
  private final StarDef _def;

  public Star(Timeline timeline, StarDef def) {
    _timeline = timeline;
    _def = def;
  }
  
  public Timeline timeline() { return _timeline; }
  public StarDef def() { return _def; }

  // delegate to def
  public Color color() { return _def.color(); }
  public double radius() { return _def.radius(); }
  public double twinklePeriod() { return _def.twinklePeriod(); }
  public double gravity() { return _def.gravity(); }

  // TODO: refactor code smell, remove this?
  public boolean dead() {
    return _timeline.end() != null;
  }
  public void destroy(double t) {
    _timeline = _timeline.limit(null, _timeline.at(t));
  }
}


package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Image;
import com.blevinstein.sr.Velocity;

import java.awt.Color;

public class StarImage {
  private final StarDef _def;
  private final Image _image;

  public StarImage(StarDef def, Image image) {
    _def = def;
    _image = image;
  }

  public StarDef def() { return _def; }
  public Image image() { return _image; }

  // delegate to StarDef
  public Color color() { return _def.color(); }
  public double radius() { return _def.radius(); }
  public double twinklePeriod() { return _def.twinklePeriod(); }
  public double gravity() { return _def.gravity(); }

  // delegate to Image
  public Event source() { return _image.source(); }
  public Event offset() { return _image.offset(); }
  public Event projected() { return _image.projected(); }
  public Velocity vObserver() { return _image.vObserver(); }
  public Velocity vRelative() { return _image.vRelative(); }
  public Velocity vSource() { return _image.vSource(); }
  public double properTime() { return _image.properTime(); }
}


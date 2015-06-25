package com.blevinstein.sr.asteroids;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic, empty Galaxy, where you manually add Stars.
 *
 * NOTE: Does not support removing Stars.
 */
public class MutableGalaxy implements Galaxy {
  private List<Star> _stars = new ArrayList<>();

  public synchronized MutableGalaxy add(Star newStar) {
    _stars.add(newStar);
    return this;
  }

  public synchronized MutableGalaxy remove(Star toRemove) {
    _stars.remove(toRemove);
    return this;
  }

  public synchronized List<Star> stars() {
    // return defensive copy
    return new ArrayList<>(_stars);
  }
}

package com.blevinstein.sr.asteroids;

import java.util.List;

/**
 * Abstraction for a galaxy, which is a collection of Stars.
 */
public interface Galaxy {
  List<Star> stars();
}


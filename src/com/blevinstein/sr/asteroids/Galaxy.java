package com.blevinstein.sr.asteroids;

import java.util.List;

/**
 * Abstraction for a galaxy, which is a collection of Stars.
 *
 * NOTE: decided not to implement transformation on an entire Galaxy, would require
 *   TransformedTimeline, and everything is lazily computed anyways. Will use velocity addition
 *   instead. Error = thomas precession, hopefully it's small.
 */
public interface Galaxy {
  List<Star> stars();
}


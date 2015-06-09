package com.blevinstein.sr.asteroids;

import java.util.List;

/**
 * Abstraction for a galaxy, which is a collection of Stars.
 *
 * TODO: implement transformed view of an entire Galaxy, requires TransformedTimeline (although
 *   since I'm lazily computing everything anyways, precomputing the transformation of a galaxy
 *   might not even be useful)
 */
public interface Galaxy {
  List<Star> stars();
}


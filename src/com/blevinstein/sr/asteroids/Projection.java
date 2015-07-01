package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Image;
import com.blevinstein.sr.Timeline;

/**
 * Defines a projection, mapping Timelines to Images.
 */
public interface Projection {
  Image project(Timeline t);
}

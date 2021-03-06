package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.StaticTimeline;
import com.blevinstein.sr.Velocity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simple grid of stars.
 */
public class GridGalaxy extends MutableGalaxy {
  /**
   * @param w width of galaxy
   * @param h height of galaxy
   * @param dx number of x divisions
   * @param dy number of y divisions
   * @param r radius of each star
   */
  public GridGalaxy(double w, double h, int dx, int dy, double r) {
    for (int i = 0; i <= dx; i++) {
      for (int j = 0; j <= dy; j++) {
        add(new Star(new StaticTimeline(-w / 2 + w * i / dx, -h / 2 + h * j / dy),
            new StarDef(Color.getHSBColor((1f * i / dx), (1f * j / dy), 0.7f),
              r,
              10 /* twinklePeriod */)));
      }
    }
  }
}

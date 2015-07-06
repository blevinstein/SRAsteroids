package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.ConstantTimeline;
import com.blevinstein.sr.Event;
import com.blevinstein.sr.Image;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
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

  // TODO: add classes that extend MutableGalaxy, override this method
  public synchronized void create(StarDef newDef, Event start, Velocity init) {
    Timeline newTimeline = new ConstantTimeline(start, init).limit(start, null);
    add(new Star(newTimeline, newDef));
  }

  private int find(StarDef toFind) {
    for (int i = 0; i < _stars.size(); i++) {
      if (_stars.get(i).def() == toFind) {
        return i;
      }
    }
    throw new IllegalArgumentException();
  }

  public synchronized MutableGalaxy destroy(StarDef def, double atTime) {
    int index = find(def);
    Star toDestroy = _stars.get(index);

    toDestroy.setTimeline(toDestroy.timeline().limit(null, toDestroy.timeline().at(atTime)));

    return this;
  }

  public synchronized MutableGalaxy remove(StarDef toRemove) {
    int index = find(toRemove);
    _stars.remove(index);
    return this;
  }

  public boolean isDead(StarDef def) {
    return _stars.get(find(def)).timeline().end() != null;
  }

  /**
   * NOTE: if abs(heuristic(x) - heuristic(y)) > r, then x and y are not within radius r
   */
  private static double heuristic(StarImage image) {
    return (image.source().x() + image.source().y()) / Math.sqrt(2);
  }

  private static final Comparator<StarImage> heuristicOrder = new Comparator<StarImage>() {
    @Override
    public int compare(StarImage s1, StarImage s2) {
      return Double.compare(heuristic(s1), heuristic(s2));
    }
  };

  public synchronized void handleCollision(Event observer, Velocity v) {
    Projection p = new Projection() {
      public Image project(Timeline t) { return t.concurrentWith(observer, v); }
    };
    List<StarImage> stars = GalaxyImage.of(this, p).stars();

    // precalculate max radius
    double maxRadius = 0;
    for (StarImage starImage : stars) {
      if (starImage.radius() > maxRadius) {
        maxRadius = starImage.radius();
      }
    }

    // sort by heuristic
    Collections.sort(stars, heuristicOrder);

    for (int i = 0; i < stars.size(); i++) {
      StarImage star1 = stars.get(i);
      if (isDead(star1.def())) continue;
      for (int j = i + 1; j < stars.size(); j++) {
        StarImage star2 = stars.get(j);
        if (isDead(star2.def())) continue;

        // no collisions possible for star1
        if (heuristic(star2) > heuristic(star1) + maxRadius) break;

        Velocity vRelative = star1.vSource().relativeMinus(star2.vSource());
        if (collide(star1.source(), star2.source(), star1.radius() + star2.radius(),
              vRelative)) {
          double collision = (star1.source().t() + star2.source().t()) / 2;
          // HACK: add additional duration of radius / velocity to each object
          double tail1 = Math.min(star1.radius() / star1.vSource().mag(), 1);
          double tail2 = Math.min(star2.radius() / star2.vSource().mag(), 1);
          destroy(star1.def(), collision + tail1);
          destroy(star2.def(), collision + tail2);
          mergeStars(star1, star2, collision);
        }
      }
    }
  }

  private void mergeStars(StarImage star1, StarImage star2, double collision) {
    Event event1 = star1.source();
    Velocity v1 = star1.vSource();
    Event event2 = star2.source();
    Velocity v2 = star2.vSource();

    double w1 = Math.pow(star1.radius(), 2);
    double w2 = Math.pow(star2.radius(), 2);
    double f = w2 / (w1 + w2);

    Event wEvent = event1.times(1-f).plus(event2.times(f));
    Velocity wVelocity = v1.times(1-f).plus(v2.times(f));
    Color wColor = interpolate(star1.color(), star2.color(), (float) f);
    double wTwinklePeriod = star1.twinklePeriod() * (1-f) + star2.twinklePeriod() * f;

    double newRadius = Math.sqrt(Math.pow(star1.radius(), 2) + Math.pow(star2.radius(), 2));
    double newGravity = star1.gravity() + star2.gravity();

    create(new StarDef(wColor, newRadius, wTwinklePeriod, newGravity), wEvent, wVelocity);
  }

  /**
   * interpolate(a, b, 0) -> a
   * interpolate(a, b, 0.5) -> (a + b) / 2
   * interpolate(a, b, 1) -> b
   */
  private Color interpolate(Color a, Color b, float f) {
    float[] hsb_a = Color.RGBtoHSB(a.getRed(), a.getGreen(), a.getBlue(), new float[3]);
    float[] hsb_b = Color.RGBtoHSB(b.getRed(), b.getGreen(), b.getBlue(), new float[3]);

    return Color.getHSBColor(f * hsb_b[0] + (1 - f) * hsb_a[0],
        f * hsb_b[1] + (1 - f) * hsb_a[1],
        f * hsb_b[2] + (1 - f) * hsb_a[2]);
  }

  /**
   * Point-circle collision.
   */
  private boolean collide(Event point, Event cCenter, double cRadius, Velocity cVelocity) {
    if (point.equals(cCenter)) { return true; } // avoid division by zero
    // vProj = mag of velocity in direction of ship
    double vProj = cVelocity.dot(point.minus(cCenter).toUnitVelocity());
    double gamma = new Velocity(vProj, 0).gamma();
    return point.minus(cCenter).dist() < cRadius / gamma;
  }

  public synchronized List<Star> stars() {
    // return defensive copy
    return new ArrayList<>(_stars);
  }
}

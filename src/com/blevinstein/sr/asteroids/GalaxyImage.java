package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Image;

import java.util.ArrayList;
import java.util.List;

public class GalaxyImage {
  private List<StarImage> _stars;

  private GalaxyImage(List<StarImage> stars) {
    _stars = stars;
  }

  public List<StarImage> stars() { return _stars; }

  public static GalaxyImage of(Galaxy galaxy, Projection p) {
    List<StarImage> images = new ArrayList<>();
    for (Star star : galaxy.stars()) {
      Image image = p.project(star.timeline());
      images.add(new StarImage(star.def(), image));
    }
    return new GalaxyImage(images);
  }
}

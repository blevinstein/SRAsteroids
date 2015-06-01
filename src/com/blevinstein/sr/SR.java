package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public class SR {
  public static final double c = 10;
  public static final double MAX = 0.95;

  // given (bx, by) = beta = v / c
  // returns new spacetime coordinates for an event after transformation
  public static Event lorentz(Event e, Velocity v) {
    double beta_sq = v.beta_sq();
    if (beta_sq >= 1) throw new IllegalArgumentException("beta > 1");
    double gamma = v.gamma();
    double bx = v.x() / c;
    double by = v.y() / c;
    
    // avoid division by zero
    if (beta_sq == 0f) return e;

    double x = - e.t() * gamma * v.x()
              + e.x() * (1 + (gamma - 1) * (bx * bx) / beta_sq)
              + e.y() * ((gamma - 1) * (bx * by) / beta_sq);
    double y = - e.t() * gamma * by * c
              + e.x() * ((gamma - 1) * (bx * by) / beta_sq)
              + e.y() * (1 + (gamma - 1) * (by * by) / beta_sq);
    double t = e.t() * gamma
            - e.x() * gamma * bx / c
            - e.y() * gamma * by / c;

    Event r = new Event(x, y, t);
    return r;
  }

  // returns an AffineTransformation for deforming a shape
  public static AffineTransform lorentzContraction(Velocity v) {
    double beta_sq = v.beta_sq();
    double gamma = v.gamma();
    double bx = v.x() / c;
    double by = v.y() / c;

    if (beta_sq == 0) return new AffineTransform();

    try {
      return new AffineTransform(
          (1 + (gamma - 1) * (bx * bx) / beta_sq),
          ((gamma - 1) * (bx * by) / beta_sq),
          ((gamma - 1) * (bx * by) / beta_sq),
          (1 + (gamma - 1) * (by * by) / beta_sq),
          0, 0).createInverse();
    } catch (NoninvertibleTransformException e) {
      throw new RuntimeException("should not happen");
    }
  }
}

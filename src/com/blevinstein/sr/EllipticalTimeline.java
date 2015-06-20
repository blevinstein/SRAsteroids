package com.blevinstein.sr;

import static com.blevinstein.util.Trig.atanh;

import com.blevinstein.Event;
import com.blevinstein.Timeline;
import com.blevinstein.Velocity;

/**
 * Represents an elliptical orbit around another timeline.
 * // TODO: generalize to ConicTimeline
 *
 *
 * Notation:
 * "**" exponent, i.e. v**2 = "v squared"
 * "r^" and "theta^" represent polar unit vectors
 *
 * "e" eccentricity
 * "R" radius of the e=0 circle
 * "a" major_axis radius along the major axis of the ellipse
 *
 * position = (r(t), theta(t)) [polar]
 *
 * r(t) = R / (1 + e * cos(theta(t)))
 *   with R = a * (1 - e)**2
 *
 * theta(t) = complicated, sweeps equal areas in equal times
 */
public class EllipticalTimeline extends Timeline {
  private double angle_perih; // angle of perihelion (on major axis)
  private Timeline center;
  private double eccentricity; // [0, 1], e=0 circle, e=1 approaches parabola
  private double gravity;
  private double major_axis; // half length of major axis
  
  // derived values
  private double radius; // radius of e=0 circle
  private double S; // S = |v(t)| * r(t), constant over time
  private double e1, e2; // used by thetaAt

  public EllipticalTimeline(double angle_perih, Timeline center, double eccentricity,
      double gravity, double major_axis) {
    this.angle_perih = angle_perih;
    this.center = center;
    this.eccentricity = eccentricity;
    this.gravity = gravity;
    this.major_axis = major_axis;

    // check eccentricity
    if (eccentricity < 0 || eccentricity >= 1) {
      throw new InvalidArgumentException(
          String.format("eccentricity %f not in [0, 1)", eccentricity));
    }
    // TODO: check a > c * gravity / E, E = (2 / (1-e) - 1), to prevent v > c

    // calculate radius
    radius = major_axis * Math.pow(1 - eccentricity, 2);
    // calculate S
    double r0 = radius / (1 + eccentricity); // radius at perihelion
    double v0 = vAtR(r0);
    S = v0 * r0;
    // calculate e1, e2
    e1 = 1 - eccentricity;
    e2 = Math.sqrt(1 - Math.pow(eccentricity, 2));
  }

  /**
   * theta(t) = 2 atanh( sqrt(1 - e**2)/(1-e) tan(S t / (2 R**2) * sqrt(1 - e**2)) )
   *          = 2 atanh( e2/e1 tan(S t / (2 R**2) * e2) )
   *          where
   *            e1 = 1-e
   *            e2 = sqrt(1 - e**2)
   */
  public double thetaAt(double t) {
    return 2 * atanh( e2 / e1 * Math.tan(S * t / 2 / Math.pow(radius, 2) * e2));
  }

  public double rAt(double theta) {
    return radius / (1 + eccentricity * Math.cos(theta));
  }

  /**
   * v(r), velocity in terms of radius, calculated from Vis-viva equation
   *
   * v**2 = gravity * (2 / r - 1 / a)
   */
  public double vAtR(double r) {
    return Math.sqrt(gravity * (2 / r - 1 / major_axis));
  }

  public Event at(double t) {
    double properTime = _center.timeElapsed(0, t);
    double theta = thetaAt(properTime);
    double r = rAt(theta);
    Event offset = new Event(r * Math.cos(theta), r * Math.sin(theta), 0);
  }

  /**
   * dr/dtheta = d/dtheta(R /(1 + e cos(theta))) = R * e sin(theta) / (1 + e cos(theta))**2
   *   (NOTE: R / (1 + e cos(theta)) == r )
   *   = r * e sin(theta) / (1 + e cos(theta))
   *
   * dtheta/dt = omega = S / r**2
   *
   * v = r^ * dr/dtheta * dtheta/dt + theta^ * dtheta/dt
   *
   *     r^ * dr/dtheta * dtheta/dt
   *   = r^ * r * e sin(theta) / (1 + e cos(theta)) * S / r**2
   *   = r^ * e sin(theta) / (1 + e cos(theta)) * S / r
   *
   * v = r^ * e sin(theta) / (1 + e cos(theta)) * S / r + theta^ * S / r**2
   */
  public Velocity velocityAt(double t) {
    double properTime = _center.timeElapsed(0, t);
    double theta = thetaAt(properTime);
    double r = rAt(theta);
    Velocity rHat = Velocity.unit(theta);
    Velocity thetaHat = Velocity.unit(theta + Math.PI/4);
    return rHat.times(
        eccentricity * Math.sin(theta) / (1 + eccentricity * Math.cos(theta)) * S / Math.pow(r,2))
        .plus(
        thetaHat.times(S / Math.pow(r, 2)));
  }
}

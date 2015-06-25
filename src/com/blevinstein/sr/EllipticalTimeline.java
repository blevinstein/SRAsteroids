package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;
import static com.blevinstein.util.Trig.atanh;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;
/**
 * Represents an elliptical orbit around another timeline
 * // TODO: generalize to ConicTimeline
 *
 * Notation:
 * "**" exponent, ie v**2 = "v squared"
 * "r^" and "theta^" represent polar unit vectors
 *
 * "e" eccentricity
 * "G" gravity
 * "R" radius of the e=0 circle
 * "a" majorAxis radius along the major axis of the ellipse
 * "b" minorAxis
 * "n" meanMotion = 2pi/period = sqrt(G / a**3)
 * "M" meanAnomaly = n * t
 * "E" eccentricAnomaly
 *
 * position = (r(t), theta(t)) [polar]
 *
 * r(t) = R / (1 + e * cos(theta(t)))
 *   with R = a * (1 - e)**2
 *
 * theta(t) = complicated, sweeps equal areas in equal times
 */
public class EllipticalTimeline extends Timeline {
  // TODO: test eccentricities > MAX_ECCENTRICITY
  public static final double MAX_ECCENTRICITY = 0.662743; // Laplace limit

  private double angleInit; // angle at t=0
  private double anglePerih; // angle of perihelion (on major axis)
  private Timeline center;
  private double eccentricity; // [0, 1], e=0 circle, e=1 approaches parabola
  private double gravity;
  private double majorAxis; // half length of major axis
  private double t0;
  
  // derived values
  private double minorAxis; // half length of minor axis
  private double radius; // radius of e=0 circle
  private double S; // S = |v(t)| * r(t), constant over time
  private double meanMotion;
  private double meanAnomalyInit;

  public EllipticalTimeline(double angleInit, double anglePerih, Timeline center, double eccentricity,
      double gravity, double majorAxis, double t0) {
    this.anglePerih = anglePerih;
    this.angleInit = angleInit;
    this.center = center;
    this.eccentricity = eccentricity;
    this.gravity = gravity;
    this.majorAxis = majorAxis;
    this.t0 = t0;

    // check eccentricity
    if (eccentricity < 0 || eccentricity >= 1) {
      throw new IllegalArgumentException(
          String.format("eccentricity %f not in [0, 1)", eccentricity));
    }
    // v_max**2 = G (2 / (R / (1 + e)) - 1 / a)
    //    ( R = a (1 - e**2) )
    //    = G (2 / (a (1 - e**2) / (1 + e)) - 1 / a)
    //    = G (2 / (a (1 - e)) - 1 / a)
    //    = G / a (2 / (1 - e) - 1)
    // v_max**2 = G E / a, where E = (2 / (1 - e) - 1)
    // a_min = G E / c**2, so that v < c at all times
    double E = 2 / (1 - eccentricity) - 1;
    if (majorAxis <= gravity * E / (c * c)) {
      throw new IllegalArgumentException("major axis is too short");
    }

    // calculate minorAxis
    minorAxis = Math.sqrt(1 - Math.pow(eccentricity, 2)) * majorAxis;
    // calculate radius
    radius = majorAxis * (1 - Math.pow(eccentricity, 2));
    // calculate S
    double r0 = radius / (1 + eccentricity); // radius at perihelion
    double v0 = vAtR(r0);
    S = v0 * r0;
    // calculate mean motion
    meanMotion = Math.sqrt(gravity / Math.pow(majorAxis, 3));
    // calcluate mean anomaly
    meanAnomalyInit = meanAnomalyAt(angleInit);
  }

  public static EllipticalTimeline create(Timeline center, double gravity, Event p0, Velocity v0) {
    double t0 = p0.t();
    Event c = center.at(t0);
    Event r0 = p0.minus(c);
    double thetaActual = r0.toUnitVelocity().angle();
    // v0**2 = G (2 / r0 - 1 / a)
    // -> a = 1 / (2 / r0 - v0**2 / G)
    double a = 1 / (2 / r0.dist() - Math.pow(v0.mag(), 2) / gravity);
    // angular momentum h = r0 cross v0 = r0 v0 sin(phi), phi = angle between r0 and v0
    // h = sqrt((1 - e**2) a G) = sqrt(R G)
    // -> R = h**2/g
    double phi = r0.toUnitVelocity().angleTo(v0);
    double h = r0.dist() * v0.mag() * Math.sin(phi);
    double radius = Math.pow(h, 2) / gravity; // radius of e=0 circle
    // calculate eccentricity from R and a
    double e = Math.sqrt(1 - radius / a);
    // theta = angle with respect to perihelion
    // r0 = R / (1 + e cos(theta))
    // -> cos(theta) = (R - r0) / (r0 e)
    double theta = Math.acos((radius - r0.dist()) / (r0.dist() * e));
    // v0_r^ = S / (R r0) * e sin(theta)
    // S, R, r0, and e are all > 0
    // -> if v0_r^ > 0, sin(theta) > 0 and theta > 0
    // -> if v0_r^ < 0, sin(theta) < 0 and theta < 0
    double dotProduct = r0.x() * v0.x() + r0.y() * v0.y();
    if (dotProduct < 0) {
      theta = -theta;
    }
    return new EllipticalTimeline(thetaActual, thetaActual - theta, center, e, gravity, a, t0);
  }

  /**
   * Uses iterative method to solve E - e sin(E) = M = n t
   * E_0 = 0
   * E_i+1 = M + e sin(E_i)
   *
   * then x, y = a(cos(E) - e), b sin(E)
   *
   * NOTE: expects to be given time since t0
   */
  private static final double thetaAt_TOL = 0.00001;
  private static final int thetaAt_MAX_ITERS = 100;
  double thetaAt(double t) {
    double meanAnomaly = meanMotion * t + meanAnomalyInit;
    while (meanAnomaly < 0) { meanAnomaly += 2 * Math.PI; }

    // find eccentricAnomaly using iterative method
    double eccentricAnomaly = 0;
    double iters = 0;
    double error;
    do {
      double newEccentricAnomaly = meanAnomaly + eccentricity * Math.sin(eccentricAnomaly);
      error = meanAnomaly - (eccentricAnomaly - eccentricity * Math.sin(eccentricAnomaly));
      eccentricAnomaly = newEccentricAnomaly;
    } while (Math.abs(error) > thetaAt_TOL && iters++ < thetaAt_MAX_ITERS);
    return Math.atan2(minorAxis * Math.sin(eccentricAnomaly),
        majorAxis * (Math.cos(eccentricAnomaly) - eccentricity)) + anglePerih;
  }

  double meanAnomalyAt(double theta) {
    // full rotations will be added back at the end
    int rotations = (int) Math.floor((theta - anglePerih) / (2 * Math.PI));

    double r = rAt(theta);
    // coordinates with respect to center of the ellipse
    // theta' = theta - theta_0
    // x = r cos(theta') + f, f = a e
    // y = r sin(theta')
    double x = r * Math.cos(theta - anglePerih) + majorAxis * eccentricity,
        y = r * Math.sin(theta - anglePerih);

    double eccentricAnomaly = Math.atan2(y / minorAxis, x / majorAxis);
    // want [0, 2pi) instead of [-pi, pi)
    if (eccentricAnomaly < 0) { eccentricAnomaly += 2 * Math.PI; }
    
    return eccentricAnomaly - eccentricity * Math.sin(eccentricAnomaly) + rotations * 2 * Math.PI;
  }

  /**
   * Calculates inverse of thetaAt(t)
   * Used for testing purposes
   */
  double timeAt(double theta) {
    return (meanAnomalyAt(theta) - meanAnomalyInit) / meanMotion + t0;
  }

  double rAt(double theta) {
    return radius / (1 + eccentricity * Math.cos(theta - anglePerih));
  }

  /**
   * v(r), velocity in terms of radius, calculated from Vis-viva equation
   *
   * v**2 = gravity * (2 / r - 1 / a)
   */
  double vAtR(double r) {
    return Math.sqrt(gravity * (2 / r - 1 / majorAxis));
  }

  public Event at(double t) {
    double properTime = center.timeElapsed(t0, t);
    double theta = thetaAt(properTime);
    double r = rAt(theta);
    return new Event(r * Math.cos(theta), r * Math.sin(theta), 0)
        .plus(center.at(t));
  }

  /**
   * dr/dtheta = d/dtheta(R /(1 + e cos(theta))) = R * e sin(theta) / (1 + e cos(theta))**2
   *   (NOTE: R / (1 + e cos(theta)) == r )
   *   = r * e sin(theta) / (1 + e cos(theta))
   *
   * dtheta/dt = omega = S / r**2
   *
   * v = r^ * dr/dtheta * dtheta/dt + theta^ * dtheta/dt * r
   *
   *     r^ * dr/dtheta * dtheta/dt
   *   = r^ * r * e sin(theta) / (1 + e cos(theta)) * S / r**2
   *   = r^ * e sin(theta) / (1 + e cos(theta)) * S / r
   *
   * v = r^ * e sin(theta) / (1 + e cos(theta)) * S / r + theta^ * S / r**2 * r
   *   = r^ * e sin(theta) / (1 + e cos(theta)) * S / r + theta^ * S / r
   */
  public Velocity velocityAt(double t) {
    double properTime = center.timeElapsed(t0, t);
    double theta = thetaAt(properTime);
    double r = rAt(theta);
    Velocity rHat = Velocity.unit(theta);
    Velocity thetaHat = Velocity.unit(theta + Math.PI/2);
    Velocity myVelocity =
        rHat.times(
            eccentricity * Math.sin(theta - anglePerih)
            / (1 + eccentricity * Math.cos(theta - anglePerih)) * S / Math.pow(r, 2))
        .plus(
            thetaHat.times(S / r));
    return center.velocityAt(t).relativePlus(myVelocity);
  }

  public double timeElapsed(double tStart, double tEnd) {
    // NOTE: NOT ACCURATE
    // TODO: include gamma factor
    return center.timeElapsed(tStart, tEnd);
  }

  @Override
  public String toString() {
    return String.format("anglePerih=%f center=%s eccentricity=%f gravity=%f majorAxis=%f " +
        "radius=%f S=%f", anglePerih, center, eccentricity, gravity, majorAxis,
        radius, S);
  }
}


package com.blevinstein.sr;

import java.util.function.Function;

// TODO: add velocity to timeline
public abstract class Timeline {
  /**
   * Describes the position of an object over time in a particular reference frame.
   */
  public abstract Event at(double t);

  /**
   * Get the instantaneous velocity of this timeline.
   */
  public abstract Velocity velocityAt(double t);

  /**
   * @return the event which represents the beginning of this timeline
   * Should return null if the timeline has no start.
   */
  public Event start() { return null; }

  /**
   * @return the event which represents the termination of this timeline
   * Should return null if the timeline doesn't end, or the end is undetermined.
   */
  public Event end() { return null; }

  public Event bisectionMethod(Function<Event, Double> errorFunction,
      double tLow, double tHigh) {
    // bisection method
    int iterations = 0;
    double tol = 0.001f;
    double tMid = (tLow + tHigh) / 2;
    double eMid = errorFunction.apply(this.at(tMid));
    while (tHigh - tLow > tol && iterations < 1000) {
      if (eMid < 0) {
        tLow = tMid;
      } else if (eMid > 0) {
        tHigh = tMid;
      } else {
        return this.at(tMid);
      }
      tMid = (tLow + tHigh) / 2;
      eMid = errorFunction.apply(this.at(tMid));
    }
    return this.at(tMid);
  }

  /**
   * @return the value of t that minimizes errorFunction.apply(at(t))
   * @param tGuess starting point for solution search
   * @param dError approximation of d(errorFunction)/dt
   */
  public Event solve(Function<Event, Double> errorFunction, double tGuess, double dError) {
    // low and high guesses for time in original reference frame
    double tLow = tGuess, tHigh = tGuess;

    double eLow, eHigh;
    eLow = eHigh = errorFunction.apply(at(tGuess));
    // adjust high and low bounds until they are valid
    while (eHigh < 0) {
      tHigh -= eHigh / dError;
      eHigh = errorFunction.apply(at(tHigh));
    }
    while (eLow > 0) {
      tLow -= eLow / dError;
      eLow = errorFunction.apply(at(tLow));
    }

    return bisectionMethod(errorFunction, tLow, tHigh);
  }

  /**
   * @return e such that lorentz(e.relativeTo(observer), v).t() = 0
   * NOTE: naive implementation finds a solution using bisection method, can be overridden
   */
  public Event concurrentWith(Event observer, Velocity v) {
    double beta_sq = v.beta_sq();
    double gamma = v.gamma();

    // avoid division by zero
    if (beta_sq == 0f) {
      return this.at(observer.t());
    }

    Event solution = solve((Event e) -> SR.lorentz(e.relativeTo(observer), v).t(),
        observer.t(),
        gamma / 2);
    return this.contains(solution) ? solution : null;
  }

  public boolean contains(Event e) {
    if (start() != null && start().t() > e.t()) {
      // e is before start of timeline
      return false;
    }
    if (end() != null && end().t() < e.t()) {
      // e is after end of timeline
      return false;
    }
    return true;
  }
}


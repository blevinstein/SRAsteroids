package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import java.util.function.Function;

/**
 * Represents a timeline by specifying:
 *   position(t)
 *   velocity(t)
 *   timeElapsed(tStart, tEnd)
 *
 * I apologize for this being a class instead of an interface.
 * TODO: implement equals?
 * TODO: fix code smell, start() and end() are HACKy
 */
public abstract class Timeline {
  /**
   * Get the position of an object over time in a particular reference frame.
   */
  public abstract Event at(double t);

  /**
   * Get the instantaneous velocity of this timeline.
   */
  public abstract Velocity velocityAt(double t);

  /**
   * Get the 'subjective' time elapsed for this timeline over the interval [tStart, tEnd] in the
   * reference frame.
   */
  public abstract double timeElapsed(double tStart, double tEnd);

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

  /**
   * Given errorFunction(this.at(tHigh)) > 0 > errorFunction(this.at(tLow))
   * Where errorFunction is continuous
   * @return tMid such that errorFunction(this.at(tMid)) ~ 0
   */
  public double bisectionMethod(Function<Event, Double> errorFunction,
      double tLow, double tHigh) {
    // bisection method
    double tol = 0.001f;
    double tMid = (tLow + tHigh) / 2;
    double eMid = errorFunction.apply(this.at(tMid));
    while (tHigh - tLow > tol) {
      if (eMid < 0) {
        tLow = tMid;
      } else if (eMid > 0) {
        tHigh = tMid;
      } else {
        // found exact solution
        return tMid;
      }
      tMid = (tLow + tHigh) / 2;
      eMid = errorFunction.apply(this.at(tMid));
    }
    return tMid;
  }

  /**
   * @return the value of t that minimizes errorFunction(this.at(t))
   * @param tGuess starting point for solution search
   * NOTE: This function requires that errorFunction(t) is monotonically increasing w.r.t. t,
   *   i.e. timelike
   */
  private static final int ITER_MAX = 500;
  public double solve(Function<Event, Double> errorFunction, double tGuess) {
    // low and high guesses for time in original reference frame
    double tLow = tGuess, tHigh = tGuess;

    double eLow, eHigh;
    eLow = eHigh = errorFunction.apply(at(tGuess));
    int iterations = 0;
    // adjust high and low bounds until they are valid
    while (eHigh < 0) {
      if (iterations++ > ITER_MAX) {
        throw new IllegalStateException("eHigh < 0");
      }
      tHigh += Math.pow(2, iterations);
      eHigh = errorFunction.apply(at(tHigh));
    }
    iterations = 0;
    while (eLow > 0) {
      if (iterations++ > ITER_MAX) {
        throw new IllegalStateException("eLow > 0");
      }
      tLow -= Math.pow(2, iterations);
      eLow = errorFunction.apply(at(tLow));
    }

    // Short circuit if definitely out of bounds
    if (start() != null && start().t() > tHigh) { return tHigh; }
    if (end() != null && end().t() < tLow) { return tLow; }

    return bisectionMethod(errorFunction, tLow, tHigh);
  }

  /**
   * @return event e such that e = lorentz(this.at(_).minus(observer), v) and e.t() == 0
   * i.e. the event on this timeline that is simultaneous with the observer in the
   *     accelerated frame of reference
   * NOTE: naive implementation finds a solution using bisection method, can be overridden
   */
  public Image concurrentWith(Event observer, Velocity v) {
    double solution = solve((Event e) -> SR.lorentz(e.minus(observer), v).t(),
        observer.t());

    return this.contains(solution)
        ? new Image(this.at(solution), this.velocityAt(solution), observer, v)
        : null;
  }

  /**
   * @return Image i such that
   *   {@code i.offset().interval() == 0 && ei.offset().t() < observer.t()}
   *   i.e. a lightlike interval between this timeline and the observer, extending from the
   *   observer into the past
   */
  public Image seenBy(Event observer, Velocity v) {
    double solution = solve((Event e) -> {
          Event image = SR.lorentz(e.minus(observer), v);
          // Calculates the time (relative to observer.t()) at which the observer sees an event
          return image.t() + image.dist() / c;
        },
        observer.t() - this.at(observer.t()).minus(observer).dist() / c);

    return this.contains(solution)
        ? new Image(this.at(solution), this.velocityAt(solution), observer, v)
        : null;
  }

  /**
   * @return Image i such that
   *   {@code i.offset().interval() == 0 && i.offset().t() > observer.t()}
   *   i.e. a lightlike interval between this timeline and the observer, extending from the
   *   observer into the future
   */
  public Image willSee(Event observer, Velocity v) {
    double solution = solve((Event e) -> {
          Event image = SR.lorentz(e.minus(observer), v);
          // Calculates the time (relative to observer.t()) at which the event sees the observer
          return image.t() - image.dist() / c;
        },
        observer.t() + this.at(observer.t()).minus(observer).dist() / c);

    return this.contains(solution)
        ? new Image(this.at(solution), this.velocityAt(solution), observer, v)
        : null;
  }

  public boolean contains(double t) {
    return (start() == null || start().t() <= t)
      && (end() == null || t <= end().t());
  }

  public LimitedTimeline limit(Event start, Event end) {
    return new LimitedTimeline(this, start, end);
  }
}


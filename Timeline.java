
import java.util.function.Function;

public abstract class Timeline {
  public abstract Event at(float t);

  public Event bisectionMethod(Function<Event, Float> errorFunction, float tLow, float tHigh) {
    // bisection method
    int iterations = 0;
    float tol = 0.001f;
    float tMid = (tLow + tHigh) / 2;
    float eMid = errorFunction.apply(this.at(tMid));
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
  public Event solve(Function<Event, Float> errorFunction, float tGuess, float dError) {
    // low and high guesses for time in original reference frame
    float tLow = tGuess, tHigh = tGuess;

    float eLow, eHigh;
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
   * @return e such that lorentz(e.relativeTo(observer), bx, by).t = 0
   * NOTE: naive implementation finds a solution using bisection method, can be overridden
   */
  public Event concurrentWith(Event observer, float bx, float by) {
    Check.checkBeta(bx, by);

    float beta_sq = bx * bx + by * by;
    float gamma = (float) (1 / Math.sqrt(1 - beta_sq));

    // avoid division by zero
    if (beta_sq == 0f) {
      return this.at(observer.t);
    }

    return solve((Event e) -> SR.lorentz(e.relativeTo(observer), bx, by).t,
        observer.t,
        gamma / 2);
  }
}


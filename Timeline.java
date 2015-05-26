
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

  // @return e such that lorentz(e.relativeTo(observer), bx, by).t = 0
  // NOTE: naive implementation finds a solution using bisection method
  public Event concurrentWith(Event observer, float bx, float by) {
    Check.checkBeta(bx, by);

    float beta_sq = bx * bx + by * by;
    float gamma = (float) (1 / Math.sqrt(1 - beta_sq));

    // avoid division by zero
    if (beta_sq == 0f) {
      return this.at(observer.t);
    }

    // low and high guesses for time in original reference frame
    float tLow = observer.t;
    float tHigh = observer.t;

    // crude approximation for dError/dt
    // intentionally low by factor 1/2 to encourage overshoot and start bisection method
    float dError = gamma / 2;

    float eLow, eHigh;
    // start with observer.t as a guess
    eLow = eHigh = SR.lorentz(this.at(observer.t).relativeTo(observer), bx, by).t;
    // adjust high and low bounds until they are valid
    while (eHigh < 0) {
      tHigh -= eHigh / dError;
      eHigh = SR.lorentz(this.at(tHigh).relativeTo(observer), bx, by).t;
    }
    while (eLow > 0) {
      tLow -= eLow / dError;
      eLow = SR.lorentz(this.at(tLow).relativeTo(observer), bx, by).t;
    }

    return bisectionMethod((Event e) -> SR.lorentz(e.relativeTo(observer), bx, by).t,
        tLow,
        tHigh);
  }
}


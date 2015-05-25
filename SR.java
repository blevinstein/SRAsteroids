

public class SR {
  public static final float c = 10;

  // given (bx, by) = beta = v / c
  // returns new spacetime coordinates for an event after transformation
  public static Event lorentz(Event e, float bx, float by) {
    Check.checkBeta(bx, by);
    Check.checkEvent(e);
    
    float beta_sq = bx * bx + by * by;
    float gamma = (float) (1 / Math.sqrt(1 - beta_sq));
    
    // avoid division by zero
    if (beta_sq == 0f) return e;

    float x = - e.t * gamma * bx * c
              + e.x * (1 + (gamma - 1) * (bx * bx) / beta_sq)
              + e.y * ((gamma - 1) * (bx * by) / beta_sq);
    float y = - e.t * gamma * by * c
              + e.x * ((gamma - 1) * (bx * by) / beta_sq)
              + e.y * (1 + (gamma - 1) * (by * by) / beta_sq);
    float t = e.t * gamma
            - e.x * gamma * bx / c
            - e.y * gamma * by / c;

    Event r = new Event(x, y, t);
    Check.checkEvent(r);
    return r;
  }
}
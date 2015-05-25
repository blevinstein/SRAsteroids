

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public class SR {
  public static final float c = 10;

  // given (bx, by) = beta = v / c
  // returns new spacetime coordinates for an event after transformation
  public static Event lorentz(Event e, float bx, float by) {
    Check.checkBeta(bx, by);
    
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
    return r;
  }

  // returns an AffineTransformation for deforming a shape
  public static AffineTransform lorentzContraction(float bx, float by) {
    float beta_sq = bx * bx + by * by;
    float gamma = (float) (1 / Math.sqrt(1 - beta_sq));

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

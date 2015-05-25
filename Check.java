

public class Check {
  public static void checkBeta(float bx, float by) {
    if (bx * bx + by * by > 1) throw new IllegalArgumentException("|beta| > 1");
  }

  public static void checkEvent(Event e) {
    if (Float.isNaN(e.x)
        || Float.isNaN(e.y)
        || Float.isNaN(e.t)) {
      System.err.println("bad event " + e);
      System.exit(0);
    }
  }
}

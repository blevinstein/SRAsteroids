package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.ArbitraryTimeline;
import com.blevinstein.sr.ConstantTimeline;
import com.blevinstein.sr.Event;
import com.blevinstein.sr.Image;
import com.blevinstein.sr.SR;
import com.blevinstein.sr.StaticTimeline;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class SRAsteroids {
  public enum ProjectionMode {
    NO_TRANSFORM,
    CONCURRENT_WITH,
    SEEN_BY
  }

  public static final double dt = 0.1;

  private ManualPilot manualPilot;
  private AutoPilot autoPilot;

  private MutableGalaxy galaxy;
  private Event observer;
  private Velocity velocity;
  private double angle;
  private double zoom;
  private Velocity lastBoost;
  private ProjectionMode mode = ProjectionMode.SEEN_BY;

  private View view;
  private KeyInput keyInput;
  private MouseInput mouseInput;

  // feature flags
  private boolean enableStarCollision = true;

  public SRAsteroids() {
    reset();
  }

  public void reset() {
    //galaxy = new UniformBubbleGalaxy(1E4, 1E-5);
    //galaxy = new CircleGalaxy(1E4, 5E-6, 1E7);
    galaxy = new SolarSystemGalaxy(2E3, 10 /* planets */, 20 /* moons */, 1E6);
    //galaxy = new GridGalaxy(1E4, 1E4, 100, 100, 10);

    observer = Event.ORIGIN;
    velocity = Velocity.ZERO;
    angle = 0;
    zoom = 1;
    lastBoost = null;

    autoPilot = null;
  }

  public SRAsteroids setView(View view) {
    this.view = view;
    return this;
  }

  public SRAsteroids setKeyInput(KeyInput keyInput) {
    this.keyInput = keyInput;
    this.manualPilot = new ManualPilot(keyInput);
    return this;
  }

  public SRAsteroids setMouseInput(MouseInput mouseInput) {
    this.mouseInput = mouseInput;
    return this;
  }

  // NOTE: synchronized draw() and mainLoop()
  public synchronized void mainLoop() {
    Velocity lastVelocity = velocity;

    // Accelerate and rotate ship
    Pilot activePilot = autoPilot != null && !autoPilot.done() ? autoPilot : manualPilot;
    Pair<Velocity, Double> newVelocityAngle = activePilot.steer(new ShipState(observer, velocity, angle));
    velocity = newVelocityAngle.getLeft().checked(0.999);
    angle = newVelocityAngle.getRight();

    lastBoost = velocity.relativeMinus(lastVelocity); // use as flag to render boost

    // Move ship
    observer = observer.plus(velocity.over(dt * velocity.gamma()));

    // Zoom view
    if (keyInput.getKeyDown(KeyEvent.VK_E)) {
      zoom *= 1.05;
    }
    if (keyInput.getKeyDown(KeyEvent.VK_Q)) {
      zoom /= 1.05;
    }
    if (keyInput.getKeyDown(KeyEvent.VK_1)) {
      mode = ProjectionMode.NO_TRANSFORM;
    } else if (keyInput.getKeyDown(KeyEvent.VK_2)) {
      mode = ProjectionMode.CONCURRENT_WITH;
    } else if (keyInput.getKeyDown(KeyEvent.VK_3)) {
      mode = ProjectionMode.SEEN_BY;
    }
    view.setZoom(zoom);

    // Handle mouse input
    Image cursorImage = getEvent(view.getImageOnScreen(mouseInput.x(), mouseInput.y()));
    ConstantTimeline target = new ConstantTimeline(cursorImage.source(), velocity);
    for (MouseEvent e : mouseInput.events()) {
      switch (e.getID()) {
        case MouseEvent.MOUSE_RELEASED:
          if (e.getButton() == MouseEvent.BUTTON3) {
            // Mouse button 3 => autopilot to location
            autoPilot = new AutoPilot(target);
          } else {
            autoPilot = null;
          }
          break;
      }
    }

    if (enableStarCollision) {
      galaxy.handleCollision(observer, velocity);
    }
  }

  public interface Pilot {
    /**
     * Given the current position/velocity/angle of the ship, accelerates and rotates.
     * @return new velocity and new angle
     */
    Pair<Velocity, Double> steer(ShipState ss);
  }

  public interface View {
    /**
     * Sets the zoom factor.
     */
    void setZoom(double zoom);

    /**
     * @param x horizontal screen coordinate from 0 to 1
     * @param y vertical screen coordinate from 0 to 1
     */
    Event getImageOnScreen(double x, double y);

    /**
     * Draw a ship around an image on screen.
     * @param angle of the ship
     */
    void ship(Color color, Image position, double angle);

    /**
     * Draw a line from p1 to p2.
     */
    void line(Color c1, Color c2, Image p1, Image p2);

    /**
     * Draw a circle around an image on screen.
     */
    void circle(Color color, Image center, double radius, boolean fill);

    /**
     * @return whether an image is on-screen
     */
    boolean isOnScreen(Image image, double radius);
  }

  private static final int TRAIL_LEN = 100;
  public synchronized void draw() {
    Projection p = getProjection();
    GalaxyImage visibleGalaxy = GalaxyImage.of(galaxy, p);

    // Show the observer
    // NOTE: assumes observer exists
    view.ship(Color.GREEN, getImage(observer), angle);

    // TODO: refactor into View#ship?
    if (lastBoost != null && lastBoost.mag() > 0) {
      // Show graphics to indicate ship output
      // TODO: support a particle system where particles last > 1 frame; space dust?
      double boostAngle = lastBoost.angle();
      for (int i = 0; i < 10; i++) {
        double outputAngle = boostAngle + random(-0.2, 0.2);
        Velocity vOutput = Velocity.unit(outputAngle)
            .times(random(0, 1 / zoom) * lastBoost.mag() * -1000);
        Event output = observer.relativePlus(vOutput.over(dt), velocity);
        view.line(Color.BLACK, Color.RED, getImage(observer), getImage(output));
      }
      lastBoost = null;
    }

    // Show the stars
    for (StarImage star : visibleGalaxy.stars()) {
      double twinklePhase = star.twinklePeriod() != 0 ?
        (star.properTime() / star.twinklePeriod()) % 1
        : 0;
      view.circle(adjust(star.color(), (float) (0.3 * Math.sin(2 * Math.PI * twinklePhase))),
          star.image(),
          star.radius(),
          true);
    }

    // Show a cursor
    Image cursorImage = Image.fromImage(view.getImageOnScreen(mouseInput.x(), mouseInput.y()), observer, velocity);
    view.circle(Color.RED, cursorImage, 50, false);

    // Show autopilot target
    if (autoPilot != null) {
      // NOTE: assumes that target exists
      Image targetImage = p.project(autoPilot.target());
      view.circle(Color.BLUE, targetImage, 10, false);
    }
  }

  /**
   * Makes a color lighter or darker.
   */
  private Color adjust(Color orig, float deltaB) {
    float[] hsb = Color.RGBtoHSB(orig.getRed(), orig.getGreen(), orig.getBlue(), new float[3]);

    // Change value, constrain to [0, 1]
    float newB = hsb[2] + deltaB;
    if (newB < 0) newB = 0;
    if (newB > 1) newB = 1;

    return Color.getHSBColor(hsb[0], hsb[1], newB);
  }

  // Projections

  public Projection noTransformProj() {
    return new Projection() {
      public Image project(Timeline t) { return t.concurrentWith(observer, Velocity.ZERO); }
    };
  }

  public Projection concurrentWithProj() {
    return new Projection() {
      public Image project(Timeline t) { return t.concurrentWith(observer, velocity); }
    };
  }

  public Projection seenByProj() {
    return new Projection() {
      public Image project(Timeline t) { return t.seenBy(observer, velocity); }
    };
  }

  /**
   * Given a Timeline, projects onto the view screen.
   */
  public Projection getProjection() {
    switch (mode) {
      case NO_TRANSFORM:
        return noTransformProj();
      case CONCURRENT_WITH:
        return concurrentWithProj();
      case SEEN_BY:
        return seenByProj();
      default:
        throw new RuntimeException();
    }
  }

  /**
   * Given an Event, projects onto the view screen.
   */
  private Image getImage(Event event) {
    return new Image(event, Velocity.ZERO, observer, velocity, 0);
  }

  /**
   * Given an Event on the view screen, project back into the world.
   */
  public Image getEvent(Event image) {
    return Image.fromImage(image, observer, velocity);
  }

  // Convenience methods

  private double random(double min, double max) {
    return (min + Math.random() * (max - min));
  }

  // Compatibility

  private static final long serialVersionUID = 1;
}


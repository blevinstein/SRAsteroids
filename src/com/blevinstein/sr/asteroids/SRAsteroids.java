package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.ArbitraryTimeline;
import com.blevinstein.sr.ConstantTimeline;
import com.blevinstein.sr.Event;
import com.blevinstein.sr.EventImage;
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
  public static final double dt = 0.1;

  private ManualPilot manualPilot;
  private AutoPilot autoPilot;

  private Galaxy galaxy;
  private Event observer;
  private Velocity velocity;
  private double angle;
  private double zoom;
  private Velocity lastBoost;

  private View view;
  private KeyInput keyInput;
  private MouseInput mouseInput;

  public SRAsteroids() {
    reset();
  }

  public void reset() {
    //galaxy = new UniformBubbleGalaxy(1E4, 1E-5);
    //galaxy = new CircleGalaxy(1E4, 5E-6, 1E7);
    //galaxy = new SolarSystemGalaxy(1E4, 10 /* planets */, 5 /* moons */, 1E7);
    galaxy = new GridGalaxy(1E4, 1E4, 100, 100, 10);

    observer = new Event(1E3 * (Math.random() - 0.5), 1E3 * (Math.random() - 0.5), 0);
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
    view.setZoom(zoom);

    // Handle mouse input
    EventImage cursorImage = getEvent(view.getImageOnScreen(mouseInput.x(), mouseInput.y()));
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

    // Handle collision
    /*
    // TODO: optimize?
    boolean death = false;
    for (Star star : galaxy.stars()) {
      Event image = getImage(star.timeline());
      if (!view.isOnScreen(image, star.radius())) {
        continue;
      }
      Event event = getEvent(image);
      Velocity vObject = star.timeline().velocityAt(event.t()).relativeMinus(velocity);
      if (collide(observer, event, star.radius(), vObject)) {
        death = true;
      }
    }
    if (death) {
      reset();
    }
    */
  }

  /**
   * Point-circle collision.
   */
  private boolean collide(Event point, Event cCenter, double cRadius, Velocity cVelocity) {
    if (point.equals(cCenter)) { return true; } // avoid division by zero
    // vProj = mag of velocity in direction of ship
    double vProj = cVelocity.dot(point.minus(cCenter).toVelocity().norm());
    double gamma = new Velocity(vProj, 0).gamma();
    return point.minus(cCenter).dist() < cRadius / gamma;
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
    void ship(Color color, EventImage position, double angle);

    /**
     * Draw a line from p1 to p2.
     */
    void line(Color c1, Color c2, EventImage p1, EventImage p2);

    /**
     * Draw a circle around an image on screen.
     */
    void circle(Color color, EventImage center, double radius, boolean fill);

    /**
     * @return whether an image is on-screen
     */
    boolean isOnScreen(EventImage image, double radius);
  }

  private static final int TRAIL_LEN = 100;
  public synchronized void draw() {
    // Show the observer
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
    for (Star star : galaxy.stars()) {
      EventImage starImage = getImage(star.timeline());
      if (!view.isOnScreen(starImage, star.radius())) {
        continue;
      }
      double twinklePhase = star.twinklePeriod() != 0 ?
        (star.timeline().timeElapsed(0, starImage.source().t()) / star.twinklePeriod()) % 1
        : 0;
      view.circle(adjust(star.color(), (float) (0.2 * Math.sin(2 * Math.PI * twinklePhase))),
          starImage,
          star.radius(),
          true);
    }

    // Show a cursor
    EventImage cursorImage = EventImage.fromImage(view.getImageOnScreen(mouseInput.x(), mouseInput.y()), observer, velocity);
    view.circle(Color.RED, cursorImage, 50, false);
    // Show autopilot target
    if (autoPilot != null) {
      EventImage targetImage = getImage(autoPilot.target());
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

  /**
   * Given a Timeline, projects onto the view screen.
   * TODO: expensive - add cache, flush on each update
   */
  public EventImage getImage(Timeline t) {
    return t.seenBy(observer, velocity);
  }

  /**
   * Given an Event, projects onto the view screen.
   */
  private EventImage getImage(Event event) {
    return new EventImage(event, Velocity.ZERO, observer, velocity);
  }

  /**
   * Given an Event on the view screen, project back into the world.
   */
  public EventImage getEvent(Event image) {
    return EventImage.fromImage(image, observer, velocity);
  }

  // Convenience methods

  private double random(double min, double max) {
    return (min + Math.random() * (max - min));
  }

  // Compatibility

  private static final long serialVersionUID = 1;
}


package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.ArbitraryTimeline;
import com.blevinstein.sr.ConstantTimeline;
import com.blevinstein.sr.Event;
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
  private ArbitraryTimeline myTimeline;
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
    galaxy = new CircleGalaxy(1E4, 5E-6, 1E7);

    myTimeline = new ArbitraryTimeline();
    myTimeline.add(Event.ORIGIN.advance(-dt));
    myTimeline.add(Event.ORIGIN);

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
    Pair<Velocity, Double> newVelocityAngle = activePilot.steer(new ShipState(myTimeline.end(), velocity, angle));
    velocity = newVelocityAngle.getLeft().checked(0.999);
    angle = newVelocityAngle.getRight();

    // TODO: calculate accelerate from boost, destroy ship if too high?
    lastBoost = velocity.relativeMinus(lastVelocity); // use as flag to render boost

    // Move ship
    myTimeline.add(myTimeline.end().plus(velocity.over(dt * velocity.gamma())));

    // Zoom view
    if (keyInput.getKeyDown(KeyEvent.VK_E)) {
      zoom *= 1.05;
    }
    if (keyInput.getKeyDown(KeyEvent.VK_Q)) {
      zoom /= 1.05;
    }
    view.setZoom(zoom);

    // Handle mouse input
    Event cursorEvent = getEvent(view.getImageOnScreen(mouseInput.x(), mouseInput.y()));
    ConstantTimeline target = new ConstantTimeline(cursorEvent, velocity);
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
  }

  /**
   * Point-circle collision.
   */
  private boolean collide(Event point, Event cCenter, double cRadius, Velocity cVelocity) {
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
    void ship(Color c, Event image, double angle);

    /**
     * Draw a line from image1 to image2.
     */
    void line(Color c1, Color c2, Event image1, Event image2);

    /**
     * Draw a circle around an image on screen.
     * @param r radius of the circle
     * @param vObject velocity of object relative to the observer.
     */
    void circle(Color c, Event image, Velocity vObject, double r, boolean fill);

    /**
     * @return whether an image is on-screen
     */
    boolean isOnScreen(Event image, double radius);
  }

  // TODO: getImage is expensive, think about caching? GalaxyImage abstraction?
  private static final int TRAIL_LEN = 100;
  public synchronized void draw() {
    observer = myTimeline.end();

    // Show the observer
    view.ship(Color.GREEN, getImage(myTimeline), angle);

    // TODO: refactor into View#ship?
    if (lastBoost != null) {
      // Show graphics to indicate ship output
      // TODO: support a particle system where particles last > 1 frame; space dust?
      double boostAngle = lastBoost.angle();
      for (int i = 0; i < 10; i++) {
        double outputAngle = boostAngle + random(-0.2, 0.2);
        Event ship = myTimeline.end();
        Velocity vOutput = Velocity.unit(outputAngle)
            .times(random(0, 1 / zoom) * lastBoost.mag() * -1000);
        Event output = ship.relativePlus(vOutput.over(dt), velocity);
        view.line(Color.BLACK, Color.RED, getImage(ship), getImage(output));
      }
      lastBoost = null;
    }

    // Show the stars
    for (Star star : galaxy.stars()) {
      // NOTE: decided not to refactor -> View#circle(Star star)
      Event image = getImage(star.timeline());
      if (!view.isOnScreen(image, star.radius())) {
        continue;
      }
      Event event = getEvent(image);
      Velocity vObject = star.timeline().velocityAt(event.t()).relativeMinus(velocity);
      double twinklePhase = star.twinklePeriod() != 0 ?
        (star.timeline().timeElapsed(0, event.t()) / star.twinklePeriod()) % 1
        : 0;
      view.circle(adjust(star.color(), (float) (0.2 * Math.sin(2 * Math.PI * twinklePhase))),
          image,
          vObject,
          star.radius(),
          true);
    }

    // Show a cursor
    Event cursorImage = view.getImageOnScreen(mouseInput.x(), mouseInput.y());
    view.circle(Color.RED, cursorImage, Velocity.ZERO, 50, false);
    // Show autopilot target
    if (autoPilot != null) {
      Event targetImage = getImage(autoPilot.target());
      view.circle(Color.BLUE, targetImage, Velocity.ZERO, 10, false);
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
   */
  public Event getImage(Timeline t) {
    return t.seenByImage(observer, velocity);
  }

  /**
   * Given an Event, projects onto the view screen.
   */
  private Event getImage(Event event) {
    return SR.lorentz(event.minus(observer), velocity);
  }

  /**
   * Given an Event on the view screen, project back into the world.
   */
  public Event getEvent(Event image) {
    // NOTE: lorentz(e - o, v) = i -> lorentz(i, -v) = e - o
    return SR.lorentz(image, velocity.times(-1)).plus(observer);
  }

  // Convenience methods

  private double random(double min, double max) {
    return (min + Math.random() * (max - min));
  }

  // Compatibility

  private static final long serialVersionUID = 1;
}


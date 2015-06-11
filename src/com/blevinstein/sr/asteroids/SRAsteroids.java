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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class SRAsteroids {
  private Galaxy galaxy = new UniformBubbleGalaxy(1E4, 1E-5, true);
  private Pilot pilot;
  private ArbitraryTimeline myTimeline = new ArbitraryTimeline();
  private Event observer = Event.ORIGIN;
  private Velocity velocity = new Velocity(0, 0);
  private double angle = 0;
  private double zoom = 1.0;
  private Velocity lastBoost = null;

  private View view;
  private KeyInput keyInput;
  // TODO: add mouse input

  public static final double dt = 0.1;

  public SRAsteroids() {
    myTimeline.add(Event.ORIGIN.advance(-dt));
    myTimeline.add(Event.ORIGIN);
  }

  public SRAsteroids setView(View view) {
    this.view = view;
    return this;
  }

  public SRAsteroids setKeyInput(KeyInput keyInput) {
    this.keyInput = keyInput;
    this.pilot = new ManualPilot(keyInput);
    return this;
  }

  // NOTE: synchronized draw() and mainLoop()
  public synchronized void mainLoop() {
    // Acceleration and rotation
    Velocity lastVelocity = velocity;
    Pair<Velocity, Double> newVelocityAngle = pilot.steer(myTimeline.end(), velocity, angle);
    velocity = newVelocityAngle.getLeft().checked(0.999);
    angle = newVelocityAngle.getRight();
    lastBoost = velocity.relativeMinus(lastVelocity); // use as flag to render boost

    // Zoom
    if (keyInput.getKeyDown(KeyEvent.VK_E)) {
      zoom *= 1.05;
    }
    if (keyInput.getKeyDown(KeyEvent.VK_Q)) {
      zoom /= 1.05;
    }
    view.setZoom(zoom);

    // Update timeline
    myTimeline.add(myTimeline.end().plus(velocity.over(dt * velocity.gamma())));
  }

  public interface Pilot {
    /**
     * Given the current position/velocity/angle of the ship, accelerates and rotates.
     * @return new velocity and new angle
     */
    Pair<Velocity, Double> steer(Event myPosition, Velocity myVelocity, double myAngle);
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
    void circle(Color c, Event image, Velocity vObject, double r);

    /**
     * @return whether an image is on-screen
     */
    boolean isOnScreen(Event image);
  }

  private static final int TRAIL_LEN = 100;
  public synchronized void draw() {
    observer = myTimeline.end();

    // Show the observer
    view.ship(Color.GREEN, getImage(myTimeline), angle);

    if (lastBoost != null) {
      // Show graphics to indicate ship output
      // TODO: support a particle system where particles last > 1 frame; space dust?
      double boostAngle = lastBoost.angle();
      for (int i = 0; i < 10; i++) {
        double outputAngle = boostAngle + random(-0.2, 0.2);
        Event ship = myTimeline.end();
        Velocity vOutput = Velocity.unit(outputAngle)
            .times(random(0, 1) * lastBoost.mag() * -1000);
        Event output = ship.relativePlus(vOutput.over(dt), velocity);
        view.line(Color.BLACK, Color.RED, getImage(ship), getImage(output));
      }
      lastBoost = null;
    }

    // Show the stars
    for (Star star : galaxy.stars()) {
      // NOTE: decided not to refactor -> View#circle(Star star)
      Event image = getImage(star.timeline());
      if (!view.isOnScreen(image)) {
        continue;
      }
      Event event = getEvent(image);
      Velocity vObject = star.timeline().velocityAt(event.t()).relativeMinus(velocity);
      boolean twinkle = false;
      if (star.twinklePeriod() != 0) {
        double timeElapsed = star.timeline().timeElapsed(0, event.t());
        double phase = (Math.abs(timeElapsed) / star.twinklePeriod()) % 1.0;
        if (phase < 0.05) { // 5% duty cycle
          twinkle = true;
        }
      }
      view.circle(twinkle ? Color.WHITE : star.color(),
          image,
          vObject,
          star.radius());
    }
  }

  // Projections

  /**
   * Given a Timeline, projects onto the view screen.
   */
  public Event getImage(Timeline t) {
    return t.seenBy(observer, velocity);
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


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

public class SRAsteroids {
  // TODO: abstract out World, separate from engine and driver code
  private MutableGalaxy galaxy = new MutableGalaxy();
  private ArbitraryTimeline myTimeline = new ArbitraryTimeline();
  private Velocity velocity = new Velocity(0, 0);
  private double angle = 0;
  private double zoom = 1.0;
  private Velocity lastBoost = null;

  private View view;

  public static final double dt = 0.1;

  public SRAsteroids() {
    myTimeline.add(Event.ORIGIN.advance(-dt));
    myTimeline.add(Event.ORIGIN);
  }

  public SRAsteroids setView(View view) {
    this.view = view;
    return this;
  }

  // NOTE: synchronized draw() and mainLoop()
  public synchronized void mainLoop() {
    // Accelerate
    double a = 0.25;
    double alpha = 0.1;
    if (view.getKeyDown(KeyEvent.VK_DOWN) != view.getKeyDown(KeyEvent.VK_UP)) {
      Velocity lastVelocity = velocity;
      if (view.getKeyDown(KeyEvent.VK_DOWN)) {
        velocity = velocity.relativePlus(Velocity.unit(angle).times(-a)).checked(0.999);
      } else {
        velocity = velocity.relativePlus(Velocity.unit(angle).times(a)).checked(0.999);
      }
      lastBoost = velocity.relativeMinus(lastVelocity); // use as flag to render boost
    }
    if (view.getKeyDown(KeyEvent.VK_LEFT) != view.getKeyDown(KeyEvent.VK_RIGHT)) {
      if (view.getKeyDown(KeyEvent.VK_LEFT)) {
        angle += alpha;
      } else {
        angle -= alpha;
      }
    }
    if (view.getKeyDown(KeyEvent.VK_E)) {
      zoom *= 1.05;
    }
    if (view.getKeyDown(KeyEvent.VK_Q)) {
      zoom /= 1.05;
    }
    view.setZoom(zoom);

    // TODO Experiment: add objects on click, in current reference frame
    // TODO Experiment: change speed of light, e.g. asteroids in normal time, then switch into
    //   "fast time" by setting c lower (and scaling down velocity of all objects?)
    // Add random objects
    if (random(0, 1) < 0.15) {
      Event image = view.getImageOnScreen(random(0, 1), random(0, 1));
      Event eventOffset = view.getEvent(image);
      Timeline timeline =
          new ConstantTimeline(eventOffset, Velocity.randomUnit().times(random(0, 0.5 * c)));
      Star newStar = new Star(timeline, Color.WHITE);
      galaxy.add(newStar);
    }

    myTimeline.add(myTimeline.end().plus(velocity.over(dt * velocity.gamma())));
  }

  public interface View {
    /**
     * Set the position and velocity of the reference frame.
     */
    void setObserver(Event now, Velocity v);

    /**
     * Sets the zoom factor.
     */
    void setZoom(double zoom);

    /**
     * This is the projection function used to put timelines on-screen.
     */
    Event getImage(Timeline t);

    /**
    * @param x horizontal screen coordinate from 0 to 1
    * @param y vertical screen coordinate from 0 to 1
    */
    Event getImageOnScreen(double x, double y);

    /**
     * Reverse of getImage projection, gives a point on the original timeline.
     */
    Event getEvent(Event image);

    /**
     * Draw a ship at getImage(t).
     * @param v velocity of observer relative to the ship.
     * @param angle of the ship
     */
    void ship(Color c, Timeline t, double angle);

    /**
     * Draw a line from getImage(t1) to getImage(t2).
     * @param v velocity of observer relative to the line.
     */
    void line(Color c1, Color c2, Timeline t1, Timeline t2);

    /**
     * Draw a circle around getImage(t).
     * @param r radius of the circle
     * @param v velocity of observer relative to the circle.
     */
    void circle(Color c, Timeline t, double r);

    /**
     * @return whether an image is on-screen
     */
    boolean isOnScreen(Event image);

    boolean getKeyDown(int keyCode);
  }

  private static final int TRAIL_LEN = 100;
  public synchronized void draw() {
    view.setObserver(myTimeline.end(), myTimeline.velocityAt(myTimeline.end().t()));

    // Show the observer
    view.ship(Color.GREEN, myTimeline, angle);

    if (lastBoost != null) {
      // Show graphics to indicate ship output
      // TODO: support a particle system where particles last > 1 frame; space dust?
      double boostAngle = lastBoost.angle();
      for (int i = 0; i < 10; i++) {
        double outputAngle = boostAngle + random(-0.2, 0.2);
        Event ship = myTimeline.end();
        Velocity vOutput = Velocity.unit(outputAngle).times(random(0, 1) * -200);
        Event output = ship.relativePlus(vOutput.over(dt), velocity);
        ConstantTimeline timeline1 = new ConstantTimeline(ship, velocity);
        ConstantTimeline timeline2 = new ConstantTimeline(output, velocity);
        view.line(Color.BLACK, Color.RED, timeline1, timeline2);
      }
      lastBoost = null;
    }

    // Show the stars
    for (Star star : galaxy.stars()) {
      view.circle(star.color(), star.timeline(), 10);
    }
  }

  // Convenience methods

  private double random(double min, double max) {
    return (min + Math.random() * (max - min));
  }

  // Compatibility

  private static final long serialVersionUID = 1;
}

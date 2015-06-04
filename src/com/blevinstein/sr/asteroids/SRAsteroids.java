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
  private List<Timeline> timelines = new ArrayList<>();
  private ArbitraryTimeline myTimeline = new ArbitraryTimeline();
  private Velocity velocity = new Velocity(0, 0);
  private double angle = 0;

  private View view;

  public static final double dt = 0.1f;

  public SRAsteroids() {
    myTimeline.add(Event.ORIGIN);
  }

  public SRAsteroids setView(View view) {
    this.view = view;
    return this;
  }

  // NOTE: synchronized draw() and mainLoop()
  public synchronized void mainLoop() {
    // Accelerate
    double a = 0.1f;
    double alpha = 0.1f;
    if (view.getKeyDown(KeyEvent.VK_DOWN) != view.getKeyDown(KeyEvent.VK_UP)) {
      if (view.getKeyDown(KeyEvent.VK_DOWN)) {
        velocity = velocity.relativePlus(velocity.unit(angle).times(-a));
      } else {
        velocity = velocity.relativePlus(velocity.unit(angle).times(a));
      }
    }
    if (view.getKeyDown(KeyEvent.VK_LEFT) != view.getKeyDown(KeyEvent.VK_RIGHT)) {
      if (view.getKeyDown(KeyEvent.VK_LEFT)) {
        angle += alpha;
      } else {
        angle -= alpha;
      }
    }
    velocity = velocity.checked(0.99);

    // TODO Experiment: add objects on click, in current reference frame
    // TODO Experiment: change speed of light, e.g. asteroids in normal time, then switch into
    //   "fast time" by setting c lower (and scaling down velocity of all objects?)
    // Add random objects
    if (random(0, 1) < 0.05) {
      Event eventOffset = view.getEvent(random(0, view.getWidth()),
          random(0, view.getHeight()));
      timelines.add(
          new ConstantTimeline(eventOffset,
            Velocity.randomUnit().times(random(0, c))));
    }

    // Prune objects outside of view
    for (int i = 0; i < timelines.size(); ) {
      Event image = view.getImage(timelines.get(i));
      if (isOnScreen(image)) {
        i++;
      } else {
        timelines.remove(i);
      }
    }

    myTimeline.add(myTimeline.end().plus(velocity.over(dt)));
  }

  // TODO: refactor into View, take Timeline as arg?
  private boolean isOnScreen(Event image) {
    return image.x() > -view.getWidth()/2 && image.x() < view.getWidth()/2
        && image.y() > -view.getHeight()/2 && image.y() < view.getHeight()/2;
  }

  // Estimate the maximum distance of any object visible on the screen
  private double getViewRadius() {
    double w = view.getWidth(), h = view.getHeight();
    // Only candidates are the 4 furthest corners of the screen
    // NOTE: only be necessary to try 2 corners, e.g. quadrants 1 and 2, under concurrentWith
    //   projection
    return Math.max(Math.max(Math.max(
      view.getEvent(w/2, h/2).minus(myTimeline.end()).dist(),
      view.getEvent(w/2, -h/2).minus(myTimeline.end()).dist()),
      view.getEvent(-w/2, -h/2).minus(myTimeline.end()).dist()),
      view.getEvent(-w/2, h/2).minus(myTimeline.end()).dist());
  }

  public interface View {
    /**
     * Set the position and velocity of the reference frame.
     */
    void setObserver(Event now, Velocity v);

    /**
     * This is the projection function used to put timelines on-screen.
     */
    Event getImage(Timeline t);

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
    void line(Color c, Timeline t1, Timeline t2);

    /**
     * Draw a circle around getImage(t).
     * @param r radius of the circle
     * @param v velocity of observer relative to the circle.
     */
    void circle(Color c, Timeline t, double r);

    /**
     * @return an Event which would be projected onto the screen at coords (x, y _)
     * @param v velocity of the observer
     */
    Event getEvent(double x, double y);

    boolean getKeyDown(int keyCode);
    int getWidth();
    int getHeight();
  }

  public synchronized void draw() {
    view.setObserver(myTimeline.end(), myTimeline.velocityAt(myTimeline.end().t()));

    // Show the observer
    view.ship(Color.WHITE, myTimeline, angle);
    List<Event> historyEvents = myTimeline.history(255);
    for (int i = 0; i < historyEvents.size() - 1; i++) {
      Event event1 = historyEvents.get(i);
      Event event2 = historyEvents.get(i + 1);
      Velocity v = event2.minus(event1).toVelocity();
      StaticTimeline trail1 = new StaticTimeline(event1.x(), event1.y());
      StaticTimeline trail2 = new StaticTimeline(event2.x(), event2.y());
      Color c = new Color(i, i, i);
      view.line(c, trail1, trail2);
    }

    // Show the objects
    for (Timeline timeline : timelines) {
      view.circle(Color.WHITE, timeline, 10);
    }
  }


  // Convenience methods

  private double random(double min, double max) {
    return (min + Math.random() * (max - min));
  }

  // Compatibility

  private static final long serialVersionUID = 1;
}

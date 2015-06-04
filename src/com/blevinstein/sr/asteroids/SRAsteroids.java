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
    double a = 1;
    double alpha = 0.1;
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
      Event image = new Event(random(-view.getWidth()/2, view.getWidth()/2),
          random(-view.getHeight()/2, view.getHeight()/2), 0);
      Event eventOffset = view.getEvent(image);
      Timeline timeline =
          new ConstantTimeline(eventOffset, Velocity.randomUnit().times(random(0, c)));
      timelines.add(timeline);
    }

    // Prune objects outside of view
    for (int i = 0; i < timelines.size(); ) {
      Event image = view.getImage(timelines.get(i));
      if (view.isOnScreen(image)) {
        i++;
      } else {
        timelines.remove(i);
      }
    }

    myTimeline.add(myTimeline.end().plus(velocity.over(dt)));
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
     * Reverse of getImage projection.
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
    void line(Color c, Timeline t1, Timeline t2);

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
    int getWidth();
    int getHeight();
  }

  private static final int TRAIL_LEN = 100;
  public synchronized void draw() {
    view.setObserver(myTimeline.end(), myTimeline.velocityAt(myTimeline.end().t()));

    // Show the observer
    view.ship(Color.WHITE, myTimeline, angle);
    List<Event> historyEvents = myTimeline.history(TRAIL_LEN);
    for (int i = 0; i < historyEvents.size() - 1; i++) {
      Event event1 = historyEvents.get(i);
      Event event2 = historyEvents.get(i + 1);
      Velocity v = event2.minus(event1).toVelocity();
      StaticTimeline trail1 = new StaticTimeline(event1.x(), event1.y());
      StaticTimeline trail2 = new StaticTimeline(event2.x(), event2.y());
      Color c = new Color(i * 1f / TRAIL_LEN, i * 1f / TRAIL_LEN, i * 1f / TRAIL_LEN);
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

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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SRAsteroids {
  private List<Timeline> timelines = new CopyOnWriteArrayList<>();
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

  public void mainLoop() {
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

    // TODO: add objects on click, in current reference frame
    // Add random objects
    if (random(0, 1) < 0.05) {
      Event eventOffset = view.getEvent(random(0, view.getWidth()),
          random(0, view.getHeight()), velocity);
      timelines.add(
          new ConstantTimeline(eventOffset, velocity));
      // remove old objects to make room
      if (timelines.size() > 100) {
        timelines.remove(0);
      }
    }
    
    myTimeline.add(myTimeline.end().plus(velocity.over(dt)));
  }

  public interface View {
    void setNow(Event now);

    void ship(Color c, Timeline t, Velocity v, double angle);
    void line(Color c, double x1, double y1, double x2, double y2, Velocity v);
    void circle(Color c, Timeline t, double r, Velocity v);

    boolean getKeyDown(int keyCode);
    Event getEvent(double x, double y, Velocity v);
    int getWidth();
    int getHeight();
  }

  public void draw() {
    view.setNow(myTimeline.end());

    // Show the observer
    view.ship(Color.WHITE, myTimeline, Velocity.ZERO, angle);
    List<Event> historyEvents = myTimeline.history(255);
    for (int i = 0; i < historyEvents.size() - 1; i++) {
      Event event1 = historyEvents.get(i);
      Event event2 = historyEvents.get(i + 1);
      Color c = new Color(i, i, i);
      view.line(c, event1.x(), event1.y(), event2.x(), event2.y(), velocity);
    }

    // Show the objects
    for (Timeline timeline : timelines) {
      view.circle(Color.WHITE, timeline, 10, velocity);
    }
  }


  // Convenience methods

  private double random(double min, double max) {
    return (min + Math.random() * (max - min));
  }

  // Compatibility

  private static final long serialVersionUID = 1;
}

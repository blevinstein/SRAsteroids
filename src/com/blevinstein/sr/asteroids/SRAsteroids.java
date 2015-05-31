package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.ConstantTimeline;
import com.blevinstein.sr.Event;
import com.blevinstein.sr.SR;
import com.blevinstein.sr.ArbitraryTimeline;
import com.blevinstein.sr.StaticTimeline;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;
import com.blevinstein.util.Throttle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;

public class SRAsteroids extends JPanel implements MouseMotionListener, KeyListener {
  private List<Timeline> timelines = new CopyOnWriteArrayList<>();
  private ArbitraryTimeline myTimeline = new ArbitraryTimeline();
  private Velocity velocity = new Velocity(0, 0);

  public static final double dt = 0.1f;

  public SRAsteroids() {
    super(null); // no layout manager

    myTimeline.add(new Event(0, 0, 0));
  }

  public void run() {
    Throttle t = new Throttle(60); // 60fps max
    while (true) {
      mainLoop();
      repaint();
      t.sleep();
    }
  }

  public void mainLoop() {
    // Accelerate
    double a = 0.1f;
    if (getKeyDown(KeyEvent.VK_DOWN) != getKeyDown(KeyEvent.VK_UP)) {
      if (getKeyDown(KeyEvent.VK_DOWN)) {
        velocity = velocity.relativePlus(velocity.norm().times(-a));
      } else {
        velocity = velocity.relativePlus(velocity.norm().times(a));
      }
    }
    if (getKeyDown(KeyEvent.VK_LEFT) != getKeyDown(KeyEvent.VK_RIGHT)) {
      if (getKeyDown(KeyEvent.VK_LEFT)) {
        velocity = velocity.relativePlus(velocity.norm().perp().times(a));
      } else {
        velocity = velocity.relativePlus(velocity.norm().perp().times(-a));
      }
    }
    velocity = velocity.checked(0.95f);

    // Add random objects
    if (random(0, 1) < 0.05) {
      Event eventOffset = new Event(random(-getWidth()/2, getWidth()/2),
          random(-getHeight()/2, getHeight()/2),
          0);
      Velocity relativeVelocity = Velocity.randomUnit().times(random(0, 1) * c);
      timelines.add(
          new ConstantTimeline(myTimeline.end().plus(eventOffset), relativeVelocity));
      // remove old objects to make room
      if (timelines.size() > 100) {
        timelines.remove(0);
      }
    }
    
    myTimeline.add(myTimeline.end().plus(velocity.over(dt)));
  }

  // Paint methods

  private void drawLine(Graphics2D g, Color c, double x1, double y1, double x2, double y2,
      Velocity v) {
    Event now = myTimeline.end();
    StaticTimeline point1 = new StaticTimeline(x1, y1);
    StaticTimeline point2 = new StaticTimeline(x2, y2);
    Event image1 = SR.lorentz(point1.concurrentWith(now, velocity).relativeTo(now), velocity);
    Event image2 = SR.lorentz(point2.concurrentWith(now, velocity).relativeTo(now), velocity);
    g.setColor(c);
    g.draw(new Line2D.Double(getWidth()/2 + image1.x(), getHeight()/2 + image1.y(),
          getWidth()/2 + image2.x(), getHeight()/2 + image2.y()));
  }

  private void fill(Graphics2D g, Color c, Timeline t, Shape s, Velocity vObserver) {
    Event now = myTimeline.end();
      // TODO: use seenBy instead of concurrentWith
    Event event = t.concurrentWith(now, vObserver);
    Velocity vObject = t.velocityAt(event.t());
    if (event == null) {
      // Timeline does not exist at this time.
      return;
    }
    Event image = SR.lorentz(event.relativeTo(now), vObserver);
    if (offScreen(image)) {
      return;
    }
    AffineTransform contraction = SR.lorentzContraction(vObserver.relativePlus(vObject));

    AffineTransform previousTransform = g.getTransform();
    g.translate(image.x() + getWidth() / 2, image.y() + getHeight() / 2);
    g.transform(contraction);
    g.setColor(c);
    g.fill(s);
    g.setTransform(previousTransform);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    BufferedImage buffer =
      new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = (Graphics2D) buffer.getGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2.setColor(Color.BLACK);
    g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));

    Event now = myTimeline.end();

    // Show the observer
    fill(g2, Color.WHITE, myTimeline, circle(2.5), Velocity.ZERO);
    double beta_sq = velocity.beta_sq();
    List<Event> historyEvents = myTimeline.history(100);
    for (int i = 0; i < historyEvents.size() - 1; i++) {
      Event event1 = historyEvents.get(i);
      Event event2 = historyEvents.get(i + 1);
      drawLine(g2, Color.WHITE, event1.x(), event1.y(), event2.x(), event2.y(), velocity);
    }

    // Show the objects
    for (Timeline timeline : timelines) {
      fill(g2, Color.WHITE, timeline, circle(10), velocity);
    }

    g.drawImage(buffer, 0, 0, null /* observer */);
  }

  // MouseMotionListener

  public void mouseDragged(MouseEvent e) {}
  public void mouseMoved(MouseEvent e) {}
  public void setPosition(MouseEvent e) {}

  // KeyListener
 
  private Map<Integer, Boolean> keys = new HashMap<>();
  public void keyPressed(KeyEvent e) {
    keys.put(e.getKeyCode(), true);
  }
  public void keyReleased(KeyEvent e) {
    keys.put(e.getKeyCode(), false);
  }
  public void keyTyped(KeyEvent e) {}
  public boolean getKeyDown(int keyCode) {
    return keys.containsKey(keyCode) && keys.get(keyCode);
  }

  // Convenience methods

  private Ellipse2D.Double circle(double r) {
    return new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
  }

  private boolean offScreen(Event e) {
    double margin = 10;
    double w = getWidth() / 2 + margin, h = getHeight() / 2 + margin;
    return e.x() > w || e.x() < -w || e.y() > h || e.y() < -h;
  }

  private double random(double min, double max) {
    return (min + Math.random() * (max - min));
  }

  // Compatibility

  private static final long serialVersionUID = 1;
}

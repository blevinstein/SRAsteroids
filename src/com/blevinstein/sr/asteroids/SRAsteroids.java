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

  public static final float dt = 0.1f;

  public SRAsteroids() {
    super(null); // no layout manager

    myTimeline.add(new Event(0, 0, 0));
  }

  // Main loop that triggers repainting
  public void run() {
    Throttle t = new Throttle(60); // 60fps max
    while (true) {
      mainLoop();
      repaint();
      t.sleep();
    }
  }

  // Core update loop
  public void mainLoop() {
    // Accelerate
    float a = 0.1f;
    if (getKeyDown(KeyEvent.VK_DOWN) != getKeyDown(KeyEvent.VK_UP)) {
      if (getKeyDown(KeyEvent.VK_DOWN)) {
        velocity = velocity.plus(velocity.norm().times(-a));
      } else {
        velocity = velocity.plus(velocity.norm().times(a));
      }
    }
    if (getKeyDown(KeyEvent.VK_LEFT) != getKeyDown(KeyEvent.VK_RIGHT)) {
      if (getKeyDown(KeyEvent.VK_LEFT)) {
        velocity = velocity.plus(velocity.norm().perp().times(a));
      } else {
        velocity = velocity.plus(velocity.norm().perp().times(-a));
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

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = (Graphics2D) buffer.getGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2.setColor(Color.BLACK);
    g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));

    Event now = myTimeline.end();

    // Show the observer
    g2.setColor(Color.WHITE);
    drawEllipse(g2, getWidth()/2, getHeight()/2, 2.5f, Velocity.ZERO);
    float beta_sq = velocity.beta_sq();
    List<Event> historyEvents = myTimeline.history(100);
    for (int i = 0; i < historyEvents.size() - 1; i++) {
      Event event1 = historyEvents.get(i);
      Event event2 = historyEvents.get(i + 1);
      StaticTimeline trail1 = new StaticTimeline(event1.x(), event1.y());
      StaticTimeline trail2 = new StaticTimeline(event2.x(), event2.y());
      // TODO: refactor so that (timeline, observer, velocity) -> image isn't so obtuse?
      Event image1 = SR.lorentz(trail1.concurrentWith(now, velocity).relativeTo(now), velocity);
      Event image2 = SR.lorentz(trail2.concurrentWith(now, velocity).relativeTo(now), velocity);
      g2.draw(new Line2D.Float(
            getWidth()/2 + image1.x(),
            getHeight()/2 + image1.y(),
            getWidth()/2 + image2.x(),
            getHeight()/2 + image2.y()));
    }

    // Show the objects
    for (Timeline timeline : timelines) {
      // TODO: use intersection on light cone instead of normal time
      Event event = timeline.concurrentWith(now, velocity);
      if (event == null) {
        // Timeline is in the past or future
        continue;
      }
      Event image = SR.lorentz(event.relativeTo(now), velocity);
      if (offScreen(image)) continue;
      // NOTE: use red = future, blue = past
      g2.setColor(new Color((int) constrain(255 + image.t(), 0, 255),
            (int) constrain(255 - Math.abs(image.t()), 0, 255),
            (int) constrain(255 - image.t(), 0, 255)));
      // TODO: make 3D to allow rotation into time
      drawEllipse(g2, image.x() + getWidth() / 2, image.y() + getHeight() / 2, 10f, velocity);
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

  private boolean offScreen(Event e) {
    float margin = 10;
    float w = getWidth() / 2 + margin, h = getHeight() / 2 + margin;
    return e.x() > w || e.x() < -w || e.y() > h || e.y() < -h;
  }

  private void drawEllipse(Graphics2D g, float x, float y, float r, Velocity v) {
    AffineTransform contraction = SR.lorentzContraction(v);

    AffineTransform previousTransform = g.getTransform();
    g.translate(x, y);
    g.transform(contraction);
    g.translate(-x, -y);
    g.draw(new Ellipse2D.Float(x - r, y - r, 2 * r, 2 * r));
    g.setTransform(previousTransform);
  }

  private float constrain(float value, float min, float max) {
    if (value < min) return min;
    if (value > max) return max;
    return value;
  }

  private float random(float min, float max) {
    return (float) (min + Math.random() * (max - min));
  }

  // Compatibility

  private static final long serialVersionUID = 1;
}

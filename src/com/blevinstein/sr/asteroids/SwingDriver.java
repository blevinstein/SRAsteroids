package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.SR;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SwingDriver extends JPanel implements SRAsteroids.View, KeyListener {

  private SRAsteroids world;
  private Event now;
  private Graphics2D graphics;

  public SwingDriver() {
    super(null); // no layout manager

    world = new SRAsteroids().setView(this);
  }

  public void run() {
    Throttle t = new Throttle(60); // 60fps max
    while (true) {
      world.mainLoop();
      repaint();
      t.sleep();
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setSize(640, 480 + 25);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    SwingDriver main = new SwingDriver();
    frame.add(main);

    frame.setVisible(true);
    frame.addKeyListener(main);

    main.run();
  }

  public void paintComponent(Graphics screenGraphics) {
    super.paintComponent(screenGraphics);

    // Create a buffer
    BufferedImage buffer =
      new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    graphics = (Graphics2D) buffer.getGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    // Clear the screen
    graphics.setColor(Color.BLACK);
    graphics.fill(new Rectangle(0, 0, getWidth(), getHeight()));

    world.draw();

    // Dump buffer to screen
    screenGraphics.drawImage(buffer, 0, 0, null /* observer */);
  }

  public void setNow(Event now) {
    this.now = now;
  }

  public void drawLine(Color c, double x1, double y1, double x2, double y2,
      Velocity v) {
    StaticTimeline point1 = new StaticTimeline(x1, y1);
    StaticTimeline point2 = new StaticTimeline(x2, y2);
    Event image1 = SR.lorentz(point1.concurrentWith(now, v).relativeTo(now), v);
    Event image2 = SR.lorentz(point2.concurrentWith(now, v).relativeTo(now), v);
    graphics.setColor(c);
    graphics.draw(new Line2D.Double(getWidth()/2 + image1.x(), getHeight()/2 + image1.y(),
          getWidth()/2 + image2.x(), getHeight()/2 + image2.y()));
  }

  public void fill(Color c, Timeline t, Shape s, Velocity vObserver) {
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

    AffineTransform previousTransform = graphics.getTransform();
    graphics.translate(image.x() + getWidth() / 2, image.y() + getHeight() / 2);
    graphics.transform(contraction);
    graphics.setColor(c);
    graphics.fill(s);
    graphics.setTransform(previousTransform);
  }

  public Event getEvent(double x, double y, Velocity v) {
    Event image = new Event(x - getWidth() / 2, y - getHeight() / 2, now.t());
    return SR.lorentz(image, v.times(-1)).plus(now);
  }

  // Convenience methods

  private boolean offScreen(Event e) {
    double margin = 10;
    double w = getWidth() / 2 + margin, h = getHeight() / 2 + margin;
    return e.x() > w || e.x() < -w || e.y() > h || e.y() < -h;
  }

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
}

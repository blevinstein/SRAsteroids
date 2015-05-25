import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;

public class SRAsteroids extends JPanel implements MouseMotionListener {
  private List<Timeline> timelines = new CopyOnWriteArrayList<>();
  private Event now = new Event(0, 0, 0);

  public static final float dt = 1;

  // mouseX/mouseY are in range [0, 1]
  private float mouseX = 0.5f;
  private float mouseY = 0.5f;

  // |beta| < 1
  private float bx = 0;
  private float by = 0;

  public SRAsteroids() {
    super(null); // no layout manager
  }

  // Main loop that triggers repainting
  public void run() {
    Throttle t = new Throttle(100); // 100fps max
    while (true) {
      mainLoop();
      repaint();
      t.sleep();
    }
  }

  public void mainLoop() {
    // Add random objects
    if (random(0, 1) < 0.05) {
      timelines.add(
          new ConstantTimeline(
              now.x + random(-getWidth()/2, getWidth()/2), now.y + random(-getHeight()/2, getHeight()/2), now.t,
              random(-0.7f, 0.7f), random(-0.7f, 0.7f)));
    }

    // Set beta < 1
    bx = 2 * (mouseX - 0.5f);
    by = 2 * (mouseY - 0.5f);
    float beta = (float) Math.sqrt(bx * bx + by * by);
    if (beta > 1) {
      bx = bx / beta * 0.99f;
      by = by / beta * 0.99f;
    }
    
    // TODO: replace (now : Event) with (self : Timeline)
    now = now.advance(bx * SR.c, by * SR.c, dt);
  }

  // Core update loop
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = (Graphics2D) buffer.getGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2.setColor(Color.BLACK);
    g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));

    // Show the observer
    float r = 2.5f;
    g2.setColor(Color.WHITE);
    drawEllipse(g2, getWidth()/2, getHeight()/2, 2.5f, 0, 0);

    // Show the objects
    for (Timeline timeline : timelines) {
      // TODO: use intersection on light cone instead of normal time
      Event event = timeline.concurrentWith(now, bx, by);
      Event image = SR.lorentz(event.relativeTo(now), bx, by);
      if (offScreen(image)) continue;
      // NOTE: use red = future, blue = past
      g2.setColor(new Color((int) constrain(255 + image.t, 0, 255),
            (int) constrain(255 - Math.abs(image.t), 0, 255),
            (int) constrain(255 - image.t, 0, 255)));
      // TODO: make 3D to allow rotation into time
      drawEllipse(g2, image.x + getWidth() / 2, image.y + getHeight() / 2, 10f, bx, by);
    }

    g.drawImage(buffer, 0, 0, null /* observer */);
  }

  // MouseMotionListener

  public void mouseDragged(MouseEvent e) {
    setPosition(e);
  }
  public void mouseMoved(MouseEvent e) {
    setPosition(e);
  }
  public void setPosition(MouseEvent e) {
    mouseX = e.getX() * 1f / getWidth();
    mouseY = e.getY() * 1f / getHeight();
  }

  // Convenience methods

  private boolean offScreen(Event e) {
    float margin = 10;
    float w = getWidth() / 2 + margin, h = getHeight() / 2 + margin;
    return e.x > w || e.x < -w || e.y > h || e.y < -h;
  }

  private void drawEllipse(Graphics2D g, float x, float y, float r, float bx, float by) {
    // TODO: show contraction in ellipse shape
    g.draw(new Ellipse2D.Float(x - r, y - r, 2 * r, 2 * r));
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

// GOAL: demonstrate space/length contractions, and behavior of relativistic spacetime
// SUCCESS: visualized length contraction

public class SRAsteroids extends JPanel {
  private List<Timeline> timelines = new ArrayList<>();
  private Event now = new Event(0, 0, 0);

  public static final float dt = 1;

  public SRAsteroids() {
    super(null); // no layout manager
  }
  
  public void run() {
    Throttle t = new Throttle(100); // 100fps max
    while (true) {
      repaint();
      t.sleep();
    }
  }
  
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    
    g2.setColor(Color.BLACK);
    g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
    
    // Add random objects
    if (random(0, 1) < 0.3) {
      timelines.add(
          new ConstantTimeline(
              now.x + random(-getWidth()/2, getWidth()/2), now.y + random(-getHeight()/2, getHeight()/2), now.t,
              random(-0.7f, 0.7f), random(-0.7f, 0.7f)));
    }
    
    // Calculate beta
    // TODO: implement MouseListener
    float bx = 0;
    float by = 0;
    //float bx = (mouseX - getWidth()/2f) / (getWidth()/2) * 0.7;
    //float by = (mouseY - getHeight()/2f) / (getHeight()/2) * 0.7;

    // Show the observer
    float r = 2.5f;
    g2.setColor(Color.WHITE);
    drawEllipse(g2, getWidth()/2, getHeight()/2, 2.5f);

    // Show the objects
    for (Timeline timeline : timelines) {
      // TODO: use intersection on light cone instead of normal time
      Event event = timeline.at(now.t);
      //Event event = timeline.concurrentWith(now, bx, by);
      Event image = SR.lorentz(event.relativeTo(now), bx, by);
      // TODO: if (!image.isOnScreen) continue;
      // NOTE: use red = future, blue = past
      g2.setColor(new Color((int) constrain(255 + image.t, 0, 255),
            (int) constrain(255 - Math.abs(image.t), 0, 255),
            (int) constrain(255 - image.t, 0, 255)));
      // TODO: make 3D to allow rotation into time
      fillEllipse(g2, image.x + getWidth() / 2, image.y + getHeight() / 2, 1f);
    }
    
    // TODO: replace (now : Event) with (self : Timeline)
    now = now.advance(bx * SR.c, by * SR.c, dt);
  }

  private void fillEllipse(Graphics2D g, float x, float y, float r) {
    g.fill(new Ellipse2D.Float(x - r, y - r, 2 * r, 2 * r));
  }
  
  private void drawEllipse(Graphics2D g, float x, float y, float r) {
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

  private static final long serialVersionUID = 1;
}

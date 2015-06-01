package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.SR;
import com.blevinstein.sr.StaticTimeline;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;
import com.blevinstein.util.Throttle;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

public class JOGLDriver implements SRAsteroids.View, KeyListener {
  
  private SRAsteroids world;
  private Event now;
  private GLCanvas canvas;
  private GL2 gl;
  private int width, height;

  public JOGLDriver() {
    world = new SRAsteroids().setView(this);
    now = Event.ORIGIN;

    GLProfile profile = GLProfile.getDefault();
    profile.initSingleton();
    GLCapabilities capabilities = new GLCapabilities(profile);
    capabilities.setSampleBuffers(true);
    capabilities.setDoubleBuffered(true);
    canvas = new GLCanvas(capabilities);
    canvas.addKeyListener(this);

    canvas.addGLEventListener(new GLEventListener() {
      @Override
      public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        width = w;
        height = h;
        setup(drawable.getGL().getGL2());
      }

      @Override
      public void init(GLAutoDrawable drawable) {
        setup(drawable.getGL().getGL2());
      }

      @Override
      public void dispose(GLAutoDrawable drawable) {
      }

      @Override
      public void display(GLAutoDrawable drawable) {
        render(drawable.getGL().getGL2());
      }
    });
  }

  public void run() {
    Throttle t = new Throttle(60); // 60fps max
    while (true) {
      world.mainLoop();
      canvas.display();
      t.sleep();
    }
  }

  public GLCanvas getCanvas() { return canvas; }

  public int getWidth() { return width; }
  public int getHeight() { return height; }

  public static void main(String[] args) {
    Frame frame = new Frame();

    JOGLDriver main = new JOGLDriver();
    frame.add(main.getCanvas());

    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        frame.remove(main.getCanvas());
        frame.dispose();
        System.exit(0);
      }
    });
    
    frame.setSize(640, 480 + 25);
    frame.setVisible(true);

    main.run();
  }

  public void setup(GL2 gl) {
    this.gl = gl;

    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();

    GLU glu = new GLU();
    glu.gluOrtho2D(0, width, 0, height);

    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glLoadIdentity();

    gl.glViewport(0, 0, width, height);
  }

  public void render(GL2 gl) {
    this.gl = gl;

    gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

    world.draw();
  }

  public void setNow(Event now) {
    this.now = now;
  }

  private void setColor(Color c) {
    gl.glColor4d(c.getRed() / 255.0,
        c.getGreen() / 255.0,
        c.getBlue() / 255.0,
        c.getAlpha() / 255.0);
  }

  private void vertex(Event e) {
    gl.glVertex2d(width/2 + e.x(), height/2 + e.y());
  }

  // TODO: use seenBy instead of concurrentWith?
  private static int SHIP_LEN = 10;
  public void ship(Color c, Timeline t, Velocity v, double angle) {
    setColor(c);
    Event image = SR.lorentz(t.concurrentWith(now, v).minus(now), v);
    Event iOffset = Velocity.unit(angle).over(1).times(SHIP_LEN);
    Event jOffset = Velocity.unit(angle).perp().over(1).times(SHIP_LEN/2);
    gl.glBegin(GL2.GL_TRIANGLE_FAN);
      vertex(image);
      vertex(image.plus(jOffset));
      vertex(image.plus(iOffset));
      vertex(image.minus(jOffset));
    gl.glEnd();
  }

  public void line(Color c, double x1, double y1, double x2, double y2, Velocity v) {
    setColor(c);
    StaticTimeline point1 = new StaticTimeline(x1, y1);
    StaticTimeline point2 = new StaticTimeline(x2, y2);
    Event image1 = SR.lorentz(point1.concurrentWith(now, v).minus(now), v);
    Event image2 = SR.lorentz(point2.concurrentWith(now, v).minus(now), v);
    gl.glLineWidth(2);
    gl.glBegin(GL2.GL_LINES);
      vertex(image1);
      vertex(image2);
    gl.glEnd();
  }

  private static int CIRCLE_SEG_LEN = 5;
  public void circle(Color c, Timeline t, double r, Velocity vObserver) {
    setColor(c);
    Event event = t.concurrentWith(now, vObserver);
    if (event == null) {
      // Timeline does not exist at this time.
      return;
    }
    Velocity vObject = t.velocityAt(event.t()).minus(vObserver);
    Event image = SR.lorentz(event.minus(now), vObject);
    // TODO if (offScreen(image)) return;

    AffineTransform contraction = SR.lorentzContraction(vObject);
    int segments = (int) Math.max(6, Math.ceil(2 * Math.PI * r / CIRCLE_SEG_LEN));
    gl.glBegin(GL2.GL_LINE_LOOP);
    for (int i = 0; i < segments; i++) {
      double x = Math.cos(2 * Math.PI * i / segments) * r;
      double y = Math.sin(2 * Math.PI * i / segments) * r;
      // Apply AffineTransform
      double xx = x * contraction.getScaleX() + y * contraction.getShearX() + contraction.getTranslateX();
      double yy = y * contraction.getScaleY() + x * contraction.getShearY() + contraction.getTranslateY();
      vertex(image.advance(xx, yy, 0));
    }
    gl.glEnd();
  }

  public Event getEvent(double x, double y, Velocity v) {
    Event image = new Event(x - width/2, y - height/2, now.t());
    return SR.lorentz(image, v.times(-1)).plus(now);
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

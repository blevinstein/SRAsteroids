package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.SR;
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
  private Event observer = Event.ORIGIN;
  private Velocity velocity = Velocity.ZERO;
  private GLCanvas canvas;
  private GL2 gl;
  private int width = 1, height = 1;
  private double zoom = 1.0;

  public JOGLDriver() {
    world = new SRAsteroids().setView(this);
    observer = Event.ORIGIN;

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

  // TODO: abstract (Event, Velocity) -> ReferenceFrame? Pair<Event, Velocity>? Observer?
  public void setObserver(Event observer, Velocity velocity) {
    this.observer = observer;
    this.velocity = velocity;
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  private void setColor(Color c) {
    gl.glColor4d(c.getRed() / 255.0,
        c.getGreen() / 255.0,
        c.getBlue() / 255.0,
        c.getAlpha() / 255.0);
  }

  private void vertex(Event image) {
    if (isOnScreen(image)) {
      gl.glVertex2d(width/2 + image.x(), height/2 + image.y());
    }
  }

  private static int SHIP_LEN = 10;
  public void ship(Color c, Timeline t, double angle) {
    setColor(c);
    Event image = getImage(t);
    // TODO: add offsets before transformation to capture lorentz contraction
    Event iOffset = Velocity.unit(angle).over(1).times(SHIP_LEN).times(zoom);
    Event jOffset = Velocity.unit(angle).perp().over(1).times(SHIP_LEN/3).times(zoom);
    gl.glBegin(GL2.GL_TRIANGLE_FAN);
      vertex(image);
      vertex(image.plus(jOffset));
      vertex(image.plus(iOffset));
      vertex(image.minus(jOffset));
    gl.glEnd();
  }

  public void line(Color c, Timeline t1, Timeline t2) {
    setColor(c);
    Event image1 = getImage(t1);
    Event image2 = getImage(t2);
    gl.glLineWidth(2);
    gl.glBegin(GL2.GL_LINES);
      vertex(image1);
      vertex(image2);
    gl.glEnd();
  }

  private static int CIRCLE_SEG_LEN = 5;
  public void circle(Color c, Timeline t, double r) {
    setColor(c);
    Event image = getImage(t);
    Event event = getEvent(image);
    Velocity vObject = t.velocityAt(event.t()).relativeMinus(velocity);

    AffineTransform contraction = SR.lorentzContraction(vObject);
    int segments = (int) Math.max(6, Math.ceil(2 * Math.PI * r / CIRCLE_SEG_LEN));

    double initAngle =  -5 * t.timeElapsed(0, event.t());

    gl.glLineWidth(2);
    gl.glBegin(GL2.GL_LINE_STRIP);
    vertex(image);
    for (int i = 0; i < segments + 1; i++) {
      double x = Math.cos(2 * Math.PI * i / segments + initAngle) * r * zoom;
      double y = Math.sin(2 * Math.PI * i / segments + initAngle) * r * zoom;
      // Apply AffineTransform
      double xx = x * contraction.getScaleX() + y * contraction.getShearX() + contraction.getTranslateX();
      double yy = y * contraction.getScaleY() + x * contraction.getShearY() + contraction.getTranslateY();
      vertex(image.advance(xx, yy, 0));
    }
    gl.glEnd();
  }

  public Event getImage(Timeline t) {
    return t.seenBy(observer, velocity).times(zoom);
  }

  public Event getEvent(Event image) {
    // NOTE: lorentz(e - o, v) = i -> lorentz(i, -v) = e - o
    return SR.lorentz(image.times(1.0 / zoom), velocity.times(-1)).plus(observer);
  }

  // TODO: public Event getImageOnScreen(double x, double y) -> Event image, depends on the
  // getImage() projection used

  public boolean isOnScreen(Event image) {
    return image.x() > -width/2 && image.x() < width/2
        && image.y() > -height/2 && image.y() < height/2;
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

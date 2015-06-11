package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.SR;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;
import com.blevinstein.util.Throttle;
import com.blevinstein.util.Ticker;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

public class JOGLDriver implements SRAsteroids.View, SRAsteroids.KeyInput, KeyListener {

  private static final int FPS = 60; // max fps
  
  private SRAsteroids world;
  private GLCanvas canvas;
  private GL2 gl;
  private TextRenderer textRenderer;
  private int width = 1, height = 1;
  private double zoom = 1.0;
  private long framerate = 0;

  public JOGLDriver() {
    world = new SRAsteroids().setView(this).setKeyInput(this);

    GLProfile profile = GLProfile.getDefault();
    GLProfile.initSingleton();
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
        TextRenderer textRenderer = new TextRenderer(new Font("SanSerif", Font.BOLD, 18));
        render(drawable.getGL().getGL2(), textRenderer);
      }
    });
  }

  public void run() {
    Throttle throttle = new Throttle(FPS);
    Ticker ticker = new Ticker();
    while (true) {
      world.mainLoop();
      canvas.display();
      framerate = ticker.tick();
      throttle.sleep();
    }
  }

  public GLCanvas getCanvas() { return canvas; }

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

  public void render(GL2 gl, TextRenderer textRenderer) {
    this.gl = gl;
    this.textRenderer = textRenderer;

    gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

    world.draw();

    textRenderer.beginRendering(width, height);
    textRenderer.setColor(Color.WHITE);
    textRenderer.draw("" + framerate, width - 100, height - 100);
    textRenderer.endRendering();
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
    gl.glVertex2d(width/2 + image.x() * zoom, height/2 + image.y() * zoom);
  }

  private static int SHIP_LEN = 10;
  public void ship(Color c, Event image, double angle) {
    setColor(c);
    // TODO: take optional velocity as argument, show lorentz contraction
    Event iOffset = Velocity.unit(angle).over(1).times(SHIP_LEN);
    Event jOffset = Velocity.unit(angle).perp().over(1).times(SHIP_LEN/3);
    gl.glBegin(GL2.GL_TRIANGLE_FAN);
      vertex(image);
      vertex(image.plus(jOffset));
      vertex(image.plus(iOffset));
      vertex(image.minus(jOffset));
    gl.glEnd();
  }

  public void line(Color c1, Color c2, Event image1, Event image2) {
    gl.glLineWidth(2);
    gl.glBegin(GL2.GL_LINES);
      setColor(c1);
      vertex(image1);
      setColor(c2);
      vertex(image2);
    gl.glEnd();
  }

  private static int CIRCLE_SEG_LEN = 5;
  public void circle(Color c, Event image, Velocity vObject, double r) {
    setColor(c);
    if (!isOnScreen(image)) { return; }

    AffineTransform contraction = SR.lorentzContraction(vObject);
    int segments = (int) Math.max(4, Math.ceil(2 * Math.PI * r / CIRCLE_SEG_LEN));

    gl.glLineWidth(2);
    gl.glBegin(GL2.GL_TRIANGLE_FAN);
    double displayRadius = Math.max(r, 1 / zoom);
    for (int i = 0; i < segments + 1; i++) {
      double x = Math.cos(2 * Math.PI * i / segments) * displayRadius;
      double y = Math.sin(2 * Math.PI * i / segments) * displayRadius;
      // Apply AffineTransform
      double xx = x * contraction.getScaleX() + y * contraction.getShearX()
        + contraction.getTranslateX();
      double yy = y * contraction.getScaleY() + x * contraction.getShearY()
        + contraction.getTranslateY();
      vertex(image.advance(xx, yy, 0));
    }
    gl.glEnd();
  }

  public Event getImageOnScreen(double x, double y) {
    double xx = (x - 0.5) * width / zoom;
    double yy = (y - 0.5) * height / zoom;
    double t = new Event(xx, yy, 0).dist() / c;
    return new Event(xx, yy, t);
  }

  // TODO: add radius to isOnScreen()
  public boolean isOnScreen(Event image) {
    return image.x() * zoom > -width/2 && image.x() * zoom < width/2
        && image.y() * zoom > -height/2 && image.y() * zoom < height/2;
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

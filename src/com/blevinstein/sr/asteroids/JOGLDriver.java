package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Image;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;

public class JOGLDriver implements SRAsteroids.View {

  private static final int FPS = 60; // max fps
  
  private SRAsteroids world;
  private GLCanvas canvas;
  private GL2 gl;
  private TextRenderer textRenderer;
  private int width = 1, height = 1;
  private double zoom = 1.0;
  private long framerate = 0;

  public JOGLDriver() {
    KeyInput keyInput = new KeyInput();
    MouseInput mouseInput = new MouseInput();
    world = new SRAsteroids()
        .setView(this)
        .setKeyInput(keyInput)
        .setMouseInput(mouseInput);

    GLProfile profile = GLProfile.getDefault();
    GLProfile.initSingleton();
    GLCapabilities capabilities = new GLCapabilities(profile);
    capabilities.setSampleBuffers(true);
    capabilities.setDoubleBuffered(true);
    canvas = new GLCanvas(capabilities);
    canvas.addKeyListener(keyInput);
    canvas.addMouseListener(mouseInput);
    canvas.addMouseMotionListener(mouseInput);
    canvas.addMouseWheelListener(mouseInput);

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

    gl.glFlush();
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
  // NOTE: ship exists at a point, is shown large for convenience, scales with zoom and is not
  //   subject to lorentz contraction
  public void ship(Color color, Image position, double angle) {
    setColor(color);
    Event iOffset = Velocity.unit(angle).over(1).times(SHIP_LEN / zoom);
    Event jOffset = Velocity.unit(angle).perp().over(1).times(SHIP_LEN/3 / zoom);
    gl.glBegin(GL2.GL_TRIANGLE_FAN);
      vertex(position.image());
      vertex(position.image().plus(jOffset));
      vertex(position.image().plus(iOffset));
      vertex(position.image().minus(jOffset));
    gl.glEnd();
  }

  public void line(Color c1, Color c2, Image p1, Image p2) {
    gl.glLineWidth(2);
    gl.glBegin(GL2.GL_LINES);
      setColor(c1);
      vertex(p1.image());
      setColor(c2);
      vertex(p2.image());
    gl.glEnd();
  }

  private static int CIRCLE_SEG_LEN = 5;
  public void circle(Color color, Image center, double radius, boolean fill) {
    setColor(color);
    if (!isOnScreen(center, radius)) { return; }

    AffineTransform contraction = center.localTransform();
    int segments = (int) Math.max(4, Math.ceil(2 * Math.PI * radius / CIRCLE_SEG_LEN));

    gl.glLineWidth(2);
    gl.glBegin(fill ? GL2.GL_TRIANGLE_FAN : GL2.GL_LINE_STRIP);
    double displayRadius = Math.max(radius, 1 / zoom);
    for (int i = 0; i < segments + 1; i++) {
      double x = Math.cos(2 * Math.PI * i / segments) * displayRadius;
      double y = Math.sin(2 * Math.PI * i / segments) * displayRadius;
      // Apply AffineTransform
      double xx = x * contraction.getScaleX() + y * contraction.getShearX()
        + contraction.getTranslateX();
      double yy = y * contraction.getScaleY() + x * contraction.getShearY()
        + contraction.getTranslateY();
      vertex(center.image().advance(xx, yy, 0));
    }
    gl.glEnd();
  }

  public Event getImageOnScreen(double x, double y) {
    double xx = (x - 0.5) * width / zoom;
    double yy = (y - 0.5) * height / zoom;
    double t = new Event(xx, yy, 0).dist() / c;
    return new Event(xx, yy, t);
  }

  public boolean isOnScreen(Image point, double radius) {
    return point.image().x() * zoom > -width/2-radius
        && point.image().x() * zoom < width/2+radius
        && point.image().y() * zoom > -height/2-radius
        && point.image().y() * zoom < height/2+radius;
  }
}


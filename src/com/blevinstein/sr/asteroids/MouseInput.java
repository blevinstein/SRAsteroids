package com.blevinstein.sr.asteroids;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * More convenient abstraction for mouse input.
 *
 * TODO: add filter for specific event ty
 */
public class MouseInput implements MouseListener, MouseMotionListener, MouseWheelListener {
  List<MouseEvent> events = new ArrayList<>();
  List<MouseWheelEvent> wheelEvents = new ArrayList<>();

  // x and y are on interval [0, 1]
  // NOTE: handles inverting y axis to match OpenGL
  private double _x = 0;
  private double _y = 0;

  public double x() { return _x; }
  public double y() { return _y; }

  public void mouseClicked(MouseEvent e) { add(e); }
  public void mouseDragged(MouseEvent e) { add(e); }
  public void mouseEntered(MouseEvent e) { add(e); }
  public void mouseExited(MouseEvent e) { add(e); }
  public void mouseMoved(MouseEvent e) { add(e); }
  public void mousePressed(MouseEvent e) { add(e); }
  public void mouseReleased(MouseEvent e) { add(e); }
  public void mouseWheelMoved(MouseWheelEvent e) { add(e); }

  // TODO: optimize, only lock add(MouseEvent)--events() and add(MouseWheelEvent)--wheelEvents()
  private synchronized void add(MouseEvent e) {
    Component screen = e.getComponent();
    _x = 1.0 * e.getX() / screen.getWidth();
    _y = 1.0 - 1.0 * e.getY() / screen.getHeight();
    events.add(e);
  }

  private synchronized void add(MouseWheelEvent e) {
    wheelEvents.add(e);
  }

  public synchronized List<MouseEvent> events() {
    List<MouseEvent> r = events;
    events = new ArrayList<>();
    return r;
  }

  public synchronized List<MouseWheelEvent> wheelEvents() {
    List<MouseWheelEvent> r = wheelEvents;
    wheelEvents = new ArrayList<>();
    return r;
  }
}

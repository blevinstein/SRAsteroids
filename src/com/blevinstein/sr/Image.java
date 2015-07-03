package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import java.awt.geom.AffineTransform;

/**
 * Represents a transformed view of an Event in another reference frame.
 */
public class Image {
  private Event _source;
  private Velocity _vSource;
  private Event _offset;
  private Velocity _vObserver;
  private Event _projected;
  private Velocity _vRelative;
  private double _properTime;

  /**
   * @param source the Event in the original reference frame
   * @param vSource the velocity of the source in the reference frame
   * @param observer the observer in the original reference frame
   * @param vObserver the velocity of the observer in the reference frame
   */
  public Image(Event source, Velocity vSource, Event observer, Velocity vObserver,
      double properTime) {
    _source = source;
    _offset = source.minus(observer);
    _projected = SR.lorentz(_offset, vObserver);
    _vSource = vSource;
    _vObserver = vObserver;
    _vRelative = vSource.relativeMinus(vObserver);
    _properTime = properTime;
  }

  public static final Image fromImage(Event image, Event observer, Velocity vObserver) {
    Event source = SR.lorentz(image, vObserver.times(-1)).plus(observer);
    return new Image(source, Velocity.ZERO, observer, vObserver, 0);
  }

  public Event source() { return _source; }
  public Event offset() { return _offset; }
  public Event projected() { return _projected; }
  public Velocity vRelative() { return _vRelative; }
  public Velocity vSource() { return _vSource; }
  public Velocity vObserver() { return _vObserver; }
  public double properTime() { return _properTime; }

  /**
   * Local transformation around the image.
   */
  public AffineTransform localTransform() {
    return SR.lorentzContraction(_vRelative);
  }
}


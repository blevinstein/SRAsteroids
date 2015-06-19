package com.blevinstein.sr;

import static com.blevinstein.sr.SR.c;

import java.awt.geom.AffineTransform;

/**
 * Represents a transformed view of an Event in another reference frame.
 */
public class EventImage {
  private Event _source;
  private Event _image;
  private Velocity _velocity;

  /**
   * @param source the Event in the original reference frame
   * @param vSource the velocity of the source in the reference frame
   * @param observer the observer in the original reference frame
   * @param vObserver the velocity of the observer in the reference frame
   */
  public EventImage(Event source, Velocity vSource, Event observer, Velocity vObserver) {
    _source = source;
    _image = SR.lorentz(source.minus(observer), vObserver);
    _velocity = vSource.relativeMinus(vObserver);
  }

  public static final EventImage fromImage(Event image, Event observer, Velocity vObserver) {
    Event source = SR.lorentz(image, vObserver.times(-1)).plus(observer);
    return new EventImage(source, Velocity.ZERO, observer, vObserver);
  }

  public Event source() { return _source; }
  public Event image() { return _image; }
  public Velocity velocity() { return _velocity; }

  /**
   * Local transformation around the image.
   */
  public AffineTransform localTransform() {
    return SR.lorentzContraction(_velocity);
  }
}


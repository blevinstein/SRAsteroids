package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Velocity;

/**
 * Container class for the state of the ship.
 */
public class ShipState {
  private final Event _position;
  private final Velocity _velocity;
  private final double _angle;

  public ShipState(Event position, Velocity velocity, double angle) {
    _position = position;
    _velocity = velocity;
    _angle = angle;
  }

  public Event position() { return _position; }
  public Velocity velocity() { return _velocity; }
  public double angle() { return _angle; }
}

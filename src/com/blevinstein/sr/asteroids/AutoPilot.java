package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;

import org.apache.commons.lang3.tuple.Pair;

// TODO: implement projection and course planning
// TODO: add way for autopilot to disengage itself
// NOTE: accelerates recklessly
public class AutoPilot implements SRAsteroids.Pilot {
  private static final double a = 0.25;

  // TODO: take optional Velocity as a ctor argument
  private Timeline _target;
  private boolean _done = false;

  public AutoPilot(Timeline target) {
    _target = target;
  }

  public Timeline target() { return _target; }

  public boolean done() { return _done; }

  public Pair<Velocity, Double> steer(Event myPosition, Velocity myVelocity, double myAngle) {
    // Naive straight-line at constant velocity, no smoothing, no anticipation
    Event event = _target.seenBy(myPosition, myVelocity); // Chase target as seen, like a dog
    if (event.minus(myPosition).dist() < 10) {
      _done = true;
      return Pair.of(myVelocity, myAngle);
    }
    Velocity desiredVelocity = event.minus(myPosition).withT(1).toVelocity().checked(0.99);
    
    // Limited acceleration
    Velocity accel = desiredVelocity.relativeMinus(myVelocity);
    if (accel.mag() > a) { accel = accel.norm().times(a); }

    // NOTE: Unlimited rotation
    return Pair.of(myVelocity.relativePlus(accel), accel.angle());
  }
}

package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;

import org.apache.commons.lang3.tuple.Pair;

// TODO: implement projection and course planning
// NOTE: accelerates recklessly
public class AutoPilot implements SRAsteroids.Pilot {
  private static final double a = 0.25;

  // TODO: take optional Velocity as a ctor argument
  private Timeline _target;
  private Event _initPosition;
  private Velocity _initVelocity;
  private boolean _done = false;

  public AutoPilot(Timeline target) {
    _target = target;
  }

  public Timeline target() { return _target; }

  public boolean done() { return _done; }

  // TODO: refactor steer() into Task/Behavior? allow AutoPilot to perform different behaviors?
  // Naive straight-line at constant velocity, no anticipation, decel to zero at target
  // NOTE: Unlimited rotation
  // TODO: refactor into separate states, accel and decel
  // TODO: refactor to return accel? is arbitrary acceleration reasonable? arbitrary acceleration
  //   might be interesting for simulating "jump to lightspeed"
  public Pair<Velocity, Double> steer(Event myPosition, Velocity myVelocity, double myAngle) {
    // One-time assignment
    if (_initPosition == null) { _initPosition = myPosition; }
    if (_initVelocity == null) { _initVelocity = myVelocity; }

    // NOTE: Chase target as seen, no anticipation
    // TODO: Anticipate target movement, move to intercept
    Event targetEvent = _target.seenBy(myPosition, myVelocity);
    // TODO: refactor code smell. projection should be handled outside the Pilot
    Event targetOffset = targetEvent.minus(myPosition);

    if (targetOffset.dist() < 100 && myVelocity.rapidity() < 1) {
      // Detect success
      _done = true;
      return Pair.of(myVelocity, myAngle);
    } else if (targetOffset.dist() < 100) {
      // Detect almost success
      Velocity accel = myVelocity.norm().times(-a);
      return Pair.of(myVelocity.relativePlus(accel), accel.angle());
    }

    // Experiment: reset initPos/Vel when stopping
    if (myVelocity.rapidity() < 15) {
      _initPosition = myPosition;
      _initVelocity = myVelocity;
    }


    double initDist = _initPosition.minus(targetEvent).dist();
    double nowDist = targetOffset.dist();
    // TODO: calculate average dt/dx/dr separately on each call to steer()
    double dt = myPosition.t() - _initPosition.t();
    double dx = nowDist - initDist;
    double dr = myVelocity.rapidity();

    // Expect dt/dx < 0, x = distance to target, x is decreasing
    double dtdx = -dt / dx;
    // Set dt/dx > 0 to avoid NaN or bad behavior
    if (Double.isNaN(dtdx) || dtdx <= 0) dtdx = 0.1;

    // Expect dt/dr > 0, r = rapidity
    double dtdr = dt / dr;
    // Set dt/dr > 0 to avoid NaN or bad behavior
    if (Double.isNaN(dtdr) || dtdr <= 0) dtdr = 0.01;

    // Calculate leanIn/leanOut boosts for accelerating and decelerating
    // NOTE: Must do withT(1) before toVelocity() because targetEvent is perceived as being in
    //   the past, so targetOffset.toVelocity() points in the wrong direction
    Velocity towardsTarget = targetOffset.withT(1).toVelocity().checked(0.99);
    Velocity leanIn = towardsTarget.relativeMinus(myVelocity).norm().times(a);
    Velocity leanOut = myVelocity.norm().times(-a);

    // Calculate time to reach target and time to decel to zero
    double timeToDecel = myVelocity.rapidity() * dtdr;
    double timeToTarget = targetOffset.dist() * dtdx;

    Velocity accel = myVelocity.rapidity() < 10 || timeToDecel <= timeToTarget ? leanIn : leanOut;
    return Pair.of(myVelocity.relativePlus(accel), accel.angle());
  }
}

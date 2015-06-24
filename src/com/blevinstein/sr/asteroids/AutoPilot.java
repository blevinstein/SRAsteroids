package com.blevinstein.sr.asteroids;

import static com.blevinstein.sr.SR.c;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Timeline;
import com.blevinstein.sr.Velocity;
import com.blevinstein.util.CsvDump;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.commons.lang3.tuple.Pair;

// TODO: implement projection and course planning
// NOTE: accelerates recklessly
public class AutoPilot implements SRAsteroids.Pilot {
  private static final double a = 0.5;

  private Timeline _target;
  private Event _initPosition;
  private Velocity _initVelocity;
  private boolean _done = false;
  private CsvDump dump;
  // TODO: analyze dumped logs

  public AutoPilot(Timeline target) {
    _target = target;

    /*
    int i = 0;
    File dumpFile;
    do {
      dumpFile = new File(String.format("log.%d.csv", i++));
    } while (dumpFile.exists());
    try {
      dump = new CsvDump(dumpFile.getPath(), "x", "y", "t", "vx", "vy");
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Failed to create dump file.");
    }
    */
  }

  public Timeline target() { return _target; }

  public boolean done() { return _done; }

  /**
   * TODO: refactor steer() into Task/Behavior? allow AutoPilot to perform different behaviors?
   * Naive straight-line at constant velocity, no anticipation, decel to zero at target
   * NOTE: Unlimited rotation
   * TODO: refactor to return accel? is arbitrary acceleration reasonable? arbitrary acceleration
   *   might be interesting for simulating "jump to lightspeed"
   */
  public Pair<Velocity, Double> steer(ShipState my) {
    /*
    dump.addRow(my.position().x(), my.position().y(), my.position().t(),
        my.velocity().x(), my.velocity().y());
    */
    // One-time assignment
    if (_initPosition == null) { _initPosition = my.position(); }
    if (_initVelocity == null) { _initVelocity = my.velocity(); }

    // NOTE: Chase target as seen, no anticipation
    // TODO: Anticipate target movement, move to intercept
    Event targetEvent = _target.seenBy(my.position(), my.velocity()).source();
    // TODO: refactor code smell. projection should be handled outside the Pilot
    Event targetOffset = targetEvent.minus(my.position());

    if (targetOffset.dist() < 100 && my.velocity().rapidity() < 1) {
      // Detect success
      _done = true;
      return Pair.of(my.velocity(), my.angle());
    } else if (targetOffset.dist() < 100) {
      // Detect almost success
      Velocity accel = my.velocity().norm().times(-a);
      return Pair.of(my.velocity().relativePlus(accel), accel.angle());
    }

    // Experiment: reset initPos/Vel when stopping
    if (my.velocity().rapidity() < 15) {
      _initPosition = my.position();
      _initVelocity = my.velocity();
    }


    double initDist = _initPosition.minus(targetEvent).dist();
    double nowDist = targetOffset.dist();
    // TODO: calculate average dt/dx/dr separately on each call to steer()
    double dt = my.position().t() - _initPosition.t();
    double dx = nowDist - initDist;
    double dr = my.velocity().rapidity();

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
    Velocity leanIn = towardsTarget.relativeMinus(my.velocity()).norm().times(a);
    Velocity leanOut = my.velocity().norm().times(-a);

    // Calculate time to reach target and time to decel to zero
    double timeToDecel = my.velocity().rapidity() * dtdr;
    double timeToTarget = targetOffset.dist() * dtdx;

    Velocity accel = my.velocity().rapidity() < 10 || timeToDecel <= timeToTarget ? leanIn : leanOut;
    return Pair.of(my.velocity().relativePlus(accel), accel.angle());
  }
}

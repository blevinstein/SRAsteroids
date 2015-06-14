package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Velocity;

import java.awt.event.KeyEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ManualPilot implements SRAsteroids.Pilot {
  private static final double a = 0.5;
  private static final double alpha = 0.1;

  private KeyInput keyInput;

  public ManualPilot(KeyInput keyInput) {
    this.keyInput = keyInput;
  }

  public Pair<Velocity, Double> steer(ShipState my) {
    Velocity newVelocity;
    if (keyInput.getKeyDown(KeyEvent.VK_DOWN)) {
      newVelocity = my.velocity().relativePlus(Velocity.unit(my.angle()).times(-a));
    } else if (keyInput.getKeyDown(KeyEvent.VK_UP)) {
      newVelocity = my.velocity().relativePlus(Velocity.unit(my.angle()).times(a));
    } else {
      newVelocity = my.velocity();
    }

    double newAngle;
    if (keyInput.getKeyDown(KeyEvent.VK_LEFT)) {
      newAngle = my.angle() + alpha;
    } else if(keyInput.getKeyDown(KeyEvent.VK_RIGHT)) {
      newAngle = my.angle() - alpha;
    } else {
      newAngle = my.angle();
    }
    
    return Pair.of(newVelocity, newAngle);
  }
}


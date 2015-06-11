package com.blevinstein.sr.asteroids;

import com.blevinstein.sr.Event;
import com.blevinstein.sr.Velocity;

import java.awt.event.KeyEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ManualPilot implements SRAsteroids.Pilot {
  private static final double a = 0.25;
  private static final double alpha = 0.1;

  private SRAsteroids.KeyInput keyInput;

  public ManualPilot(SRAsteroids.KeyInput keyInput) {
    this.keyInput = keyInput;
  }

  public Pair<Velocity, Double> steer(Event myPosition, Velocity myVelocity, double myAngle) {
    Velocity newVelocity;
    if (keyInput.getKeyDown(KeyEvent.VK_DOWN)) {
      newVelocity = myVelocity.relativePlus(Velocity.unit(myAngle).times(-a));
    } else if (keyInput.getKeyDown(KeyEvent.VK_UP)) {
      newVelocity = myVelocity.relativePlus(Velocity.unit(myAngle).times(a));
    } else {
      newVelocity = myVelocity;
    }

    double newAngle;
    if (keyInput.getKeyDown(KeyEvent.VK_LEFT)) {
      newAngle = myAngle + alpha;
    } else if(keyInput.getKeyDown(KeyEvent.VK_RIGHT)) {
      newAngle = myAngle - alpha;
    } else {
      newAngle = myAngle;
    }
    
    return Pair.of(newVelocity, newAngle);
  }
}

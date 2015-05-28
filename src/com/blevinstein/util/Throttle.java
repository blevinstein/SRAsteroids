package com.blevinstein.util;

// sleep timing logic inspired by Processing
// https://code.google.com/p/processing/source/browse/trunk/processing/android/core/src/processing/core/PApplet.java?r=7046

// USAGE
// Throttle t = new Throttle(100); // 100 frames per second
// while (true) {
//   doSomething();
//   t.sleep();
// }
public class Throttle {
  private int _rate; // rate in Hertz (= 1/sec)
  public Throttle(int rate) {
    _rate = rate;
    _beforeTime = System.nanoTime();
  }

  private long _beforeTime;
  private long _oversleep;
  // sleeps just enough to hit the target rate
  public void sleep() {
    // determine duration between sleep() calls
    long afterTime = System.nanoTime();
    long duration = afterTime - _beforeTime;

    // determine how long to wait
    long waitTime = 1000000000L / _rate - duration - _oversleep;
    if (waitTime > 0) {
      try {
        Thread.sleep(waitTime / 1000000L);
      } catch (InterruptedException e) {}
    }
    long lastTime = System.nanoTime();

    // calculate oversleep, total of all unaccounted time in last cycle
    _oversleep = lastTime - (_beforeTime + duration + waitTime);

    // update _beforeTime for next duration calculation
    _beforeTime = lastTime;
  }
}

package com.blevinstein.util;

import java.util.Arrays;

/** Simple class for tracking rate (per second) of tick() calls.
 *
 * Uses an array as a circular list, stores position in index, stores current
 *   time in ticks[index]. Each tick, index++, and calculates how long it took
 *   to complete the circuit.
 */

public class Ticker {
  // TODO: add tests, allow mocking of clock
  private int index;
  private int N;
  private long ticks[];

  /**
   * Chooses a reasonable default for tick array size.
   */
  public Ticker() {
    this(100);
  }

  /**
   * Creates a Ticker which will calculate frame rate based on a running average
   * of the last n ticks.
   */
  public Ticker(int n) {
    N = n;
    ticks = new long[N];
    index = 0;
  }

  /**
   * Registers a tick and gets tick speed in Hz.
   */
  public long tick() {
    // get next index position (circular increment)
    int newIndex = (index + 1) % N;

    // get current time
    long newTime = System.nanoTime();

    // calculate framerate from duration of this loop around the array
    long duration = newTime - ticks[newIndex];
    if (duration == 0) { duration = 1; } // HACK: prevent divide-by-zero exceptions
    long frameRate = N * 1000000000L / duration;
    ticks[newIndex] = newTime;

    // advance index position
    index = newIndex;

    return frameRate;
  }
}


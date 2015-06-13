package com.blevinstein.util;

/**
 * Class for getting the running average of many values.
 *
 * Average avg = new Average();
 * while (...) {
 *   double someValue = avg.of(someCalculation);
 * }
 *
 * Offers smoothing of estimates inside a loop.
 */
public class Average {
  private double total = 0;
  private int count = 0;

  public double of(double x) {
    total += x;
    count ++;
    return total / count;
  }
}

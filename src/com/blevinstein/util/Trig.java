package com.blevinstein.util;

public class Trig {
  public static double acosh(double x) {
    if (x < 1) { throw new IllegalArgumentException("x < 1"); }
    return Math.log(x + Math.sqrt(x * x - 1));
  }
  public static double asinh(double x) {
    return Math.log(x + Math.sqrt(x * x + 1));
  }
  public static double atanh(double x) {
    if (x < -1) { throw new IllegalArgumentException("x < -1"); }
    if (x > 1) { throw new IllegalArgumentException("x > 1"); }
    return 0.5 * Math.log((1 + x) / (1 - x));
  }
}

package com.blevinstein.util;

public class Trig {
  public static double acosh(double x) {
    return Math.log(x + Math.sqrt(x * x - 1));
  }
  public static double asinh(double x) {
    return Math.log(x + Math.sqrt(x * x + 1));
  }
  public static double atanh(double x) {
    return 0.5 * Math.log((1 + x) / (1 - x));
  }
}

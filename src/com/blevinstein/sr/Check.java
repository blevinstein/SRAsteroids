package com.blevinstein.sr;

public class Check {
  public static void checkBeta(float bx, float by) {
    if (bx * bx + by * by > 1) {
      throw new IllegalArgumentException("|beta| > 1: [" + bx + "," + by + "]");
    }
  }
}

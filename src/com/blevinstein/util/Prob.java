package com.blevinstein.util;

/**
 * Utility methods for generating random values according to particular distributions.
 */
public class Prob {
  /**
   * Poisson distribution
   * Exp[X] = Var[X] = lambda
   * P[X = k] = lambda^k * exp(-lambda) / k!
   */
  public static int poisson(double lambda) {
    double randProb = Math.random();
    double cumProb = 0;
    int k = 0;
    while (cumProb < randProb) {
      cumProb += Math.pow(lambda, k) * Math.exp(-lambda) / factorial(k);
      k++;
    }
    return k - 1;
  }

  /**
   * NOTE: long instead of int to avoid overflow, might be unnecessary
   */
  private static long factorial(long x) {
    if (x <= 1) { return 1; }
    return x * factorial(x - 1);
  }
}

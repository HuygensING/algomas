package nl.knaw.huygens.algomas;

public class ExtMath {
  /**
   * log(exp(x) + exp(y)), computed in a stable way.
   */
  public static double logAddExp(double x, double y) {
    if (x < y) {
      double t = x;
      x = y;
      y = t;
    }
    return x + Math.log1p(Math.exp(y - x));
  }

  /**
   * Standard logistic function: 1/(1 + exp(-z)).
   */
  public static double logistic(double z) {
    return .5 * (1 + Math.tanh(.5 * z));
  }
}

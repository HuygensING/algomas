package nl.knaw.huygens.algomas;

import org.junit.Test;

import static nl.knaw.huygens.algomas.ExtMath.logAddExp;
import static nl.knaw.huygens.algomas.ExtMath.logistic;
import static org.junit.Assert.assertTrue;

public class TestExtMath {
  @Test
  public void testLogAddExp() {
    assertTrue(Math.abs(logAddExp(1000, 0) - 1000) < 1e-17);
    assertTrue(Math.abs(logAddExp(0, 1000) - 1000) < 1e-17);
  }

  @Test
  public void testLogistic() {
    double prev = 0;
    for (double x = -40; x <= 40; x += 1) {
      double logist = logistic(x);
      assertTrue(logist >= prev);
      assertTrue(logist <= 1);
      prev = logist;
    }

    // For "reasonable" inputs, we want outputs that are strictly between 0 and 1.
    assertTrue(logistic(-38) > 0);
    assertTrue(logistic(37) < 1);
    assertTrue(Math.abs(logistic(0) - .5) < 1e-11);
  }
}

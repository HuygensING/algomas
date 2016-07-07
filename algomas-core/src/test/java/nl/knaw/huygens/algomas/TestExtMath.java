package nl.knaw.huygens.algomas;

import org.junit.Assert;
import org.junit.Test;

import static nl.knaw.huygens.algomas.ExtMath.logAddExp;

public class TestExtMath {
  @Test
  public void testLogAddExp() {
    Assert.assertTrue(Math.abs(logAddExp(1000, 0) - 1000) < 1e-17);
    Assert.assertTrue(Math.abs(logAddExp(0, 1000) - 1000) < 1e-17);
  }
}

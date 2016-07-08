package nl.knaw.huygens.algomas;

/*
 * #%L
 * algomas-core
 * %%
 * Copyright (C) 2016 Huygens ING (KNAW)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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

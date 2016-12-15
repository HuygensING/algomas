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

public class ExtMath {
  private ExtMath() {
  }

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

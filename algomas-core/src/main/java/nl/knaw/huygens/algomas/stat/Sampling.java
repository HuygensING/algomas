package nl.knaw.huygens.algomas.stat;

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

import java.util.Random;
import java.util.function.IntUnaryOperator;

public class Sampling {
  /**
   * Uniform random sample, without replacement, of the integers [0..n).
   *
   * @param n          Size of range to sample from.
   * @param sampleSize Size of resulting sample.
   * @param nextInt    Random number generator: nextInt.applyAsInt(k) should return
   *                   a uniform random number in [0..k).
   * @return An array of {@code sampleSize} integers, all distinct, in sorted order.
   */
  public static int[] withoutReplacement(int n, int sampleSize, IntUnaryOperator nextInt) {
    // Inspired by http://www.javamex.com/tutorials/random_numbers/random_sample.shtml,
    // but simplified.
    int[] sample = new int[sampleSize];

    for (int i = 0, nSampled = 0; sampleSize > 0; i++) {
      // With probability #(elements left) / #(elements still needed) {...}
      if (nextInt.applyAsInt(n - i) < sampleSize) {
        sample[nSampled++] = i;
        sampleSize--;
      }
    }
    return sample;
  }

  public static int[] withoutReplacement(int n, int sampleSize) {
    return withoutReplacement(n, sampleSize, new Random());
  }

  /**
   * Equivalent to Sampling.withoutReplacement(n, sampleSize, rnd::nextInt).
   */
  public static int[] withoutReplacement(int n, int sampleSize, Random rnd) {
    return withoutReplacement(n, sampleSize, rnd::nextInt);
  }
}

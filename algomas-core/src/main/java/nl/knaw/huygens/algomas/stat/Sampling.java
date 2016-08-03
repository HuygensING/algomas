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

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.function.IntUnaryOperator;

public class Sampling {
  private static class IteratorWithoutReplacement<E> implements Iterator<E> {
    private final Iterator<E> input;
    private int inputSize;
    private final IntUnaryOperator nextInt;
    private int sampleSize;

    private IteratorWithoutReplacement(Iterator<E> input, int inputSize, int sampleSize,
                                       IntUnaryOperator nextInt) {

      this.input = input;
      this.inputSize = inputSize;
      this.sampleSize = sampleSize;
      this.nextInt = nextInt;
    }

    // hasNext() and next() implement a lazy version of
    // withoutReplacement(int, int, IntUnaryOperator)'s algorithm.
    @Override
    public final boolean hasNext() {
      return sampleSize > 0;
    }

    @Override
    public final E next() {
      E result = null;
      do {
        result = input.next();
      } while (nextInt.applyAsInt(inputSize--) >= sampleSize);
      sampleSize--;
      return result;
    }
  }

  /**
   * Uniform random sample, without replacement, of the elements in the given iterator.
   *
   * @param size       Number of elements in iterator (or lower bound).
   * @param sampleSize Size of resulting sample.
   * @param nextInt    Random number generator: nextInt.applyAsInt(k) should return
   *                   a uniform random number in [0..k).
   * @return An iterator that produces a random sample of iterator's elements.
   */
  public static <E> Iterator<E> withoutReplacement(Iterator<E> iterator, int size, int sampleSize,
                                                   IntUnaryOperator nextInt) {
    return new IteratorWithoutReplacement<E>(iterator, size, sampleSize, nextInt);
  }

  /**
   * Uniform random sample, without replacement, of the elements in the given collection.
   *
   * @param sampleSize Size of resulting sample.
   * @param nextInt    Random number generator: nextInt.applyAsInt(k) should return
   *                   a uniform random number in [0..k).
   * @return An iterator that produces a random sample of collection's elements.
   */
  public static <E> Iterator<E> withoutReplacement(Collection<E> collection, int sampleSize,
                                                   IntUnaryOperator nextInt) {
    return withoutReplacement(collection.iterator(), collection.size(), sampleSize, nextInt);
  }

  public static <E> Iterator<E> withoutReplacement(Collection<E> collection, int sampleSize,
                                                   Random rnd) {
    return withoutReplacement(collection, sampleSize, rnd::nextInt);
  }

  public static <E> Iterator<E> withoutReplacement(Collection<E> collection, int sampleSize) {
    return withoutReplacement(collection, sampleSize, new Random());
  }

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

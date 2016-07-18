package nl.knaw.huygens.algomas.nlp;

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

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NGrams {
  /**
   * Generate n-grams of the items in listof length at least minN, at most maxN (inclusive).
   * <p>
   * An n-gram is a sequence of consecutive items of a list, typically a list of tokens in a text.
   * <p>
   * The n-grams are ordered first by starting position in list, then by length.
   */
  public static <T> Stream<List<T>> generate(int minN, int maxN, List<T> list) {
    if (minN > maxN) {
      throw new IllegalArgumentException("minN should be <= maxN");
    } else if (maxN <= 0) {
      throw new IllegalArgumentException("maxN should be >= 0");
    }

    return IntStream.range(0, list.size()).boxed()
      .flatMap(start -> IntStream.range(minN, Math.min(maxN, list.size() - start) + 1)
        .mapToObj(n -> list.subList(start, start + n)));
  }

  /**
   * Generate n-grams of length n from list.
   */
  public static <T> Stream<List<T>> generate(List<T> list, int n) {
    return generate(n, n, list);
  }
}
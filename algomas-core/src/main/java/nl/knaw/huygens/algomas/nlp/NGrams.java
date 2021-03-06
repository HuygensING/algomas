package nl.knaw.huygens.algomas.nlp;

/*
 * #%L
 * algomas-core
 * %%
 * Copyright (C) 2017 Huygens ING (KNAW)
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
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;

public class NGrams {
  NGrams() {
  }

  /**
   * Generate character n-grams of s, of lengths in the range [minN, maxN]
   * <p>
   * Calling {@link Stream#parallel} on this stream is safe, regardless of the type of input.
   * <p>
   * The returned CharSequences compare equal to each other if they have equal length and content;
   * i.e., {@code a.equals(b)} iff {@code a.toString().equals(b.toString()}.
   * They do not compare equal to any other CharSequences, such as Strings.
   *
   * @return Returns a sequential stream of n-grams, represented as CharSequences.
   */
  public static Stream<CharSequence> ofChars(int minN, int maxN, CharSequence s) {
    boundsCheck(minN, maxN);

    String str = s.toString();
    final int length = str.length();
    return range(0, length).boxed()
                           .flatMap(start -> range(minN, Math.min(maxN, length - start) + 1)
                             .mapToObj(n -> new StringSlice(start, start + n, str)));
  }

  /**
   * Generate n-grams of length n from s.
   */
  public static Stream<CharSequence> ofChars(int n, CharSequence s) {
    return ofChars(n, n, s);
  }

  /**
   * Generate n-grams of the items in list, of lengths in the range [minN, maxN].
   * <p>
   * An n-gram is a sequence of consecutive items of a list, typically a list of tokens in a text.
   * <p>
   * The n-grams are ordered first by starting position in list, then by length.
   * <p>
   * Calling {@link Stream#parallel} on the result is safe if calling {@link List#subList}
   * on the input list is safe.
   */
  public static <T> Stream<List<T>> generate(int minN, int maxN, List<T> list) {
    boundsCheck(minN, maxN);

    final int size = list.size();
    return range(0, size).boxed()
                         .flatMap(start -> range(minN, Math.min(maxN, size - start) + 1)
                           .mapToObj(n -> list.subList(start, start + n)));
  }

  /**
   * Generate n-grams of length n from list.
   */
  public static <T> Stream<List<T>> generate(List<T> list, int n) {
    return generate(n, n, list);
  }

  private static void boundsCheck(int minN, int maxN) {
    if (minN > maxN) {
      throw new IllegalArgumentException("minN should be <= maxN");
    } else if (maxN <= 0) {
      throw new IllegalArgumentException("maxN should be >= 0");
    }
  }
}

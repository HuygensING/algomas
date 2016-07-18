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

import nl.knaw.huygens.algomas.nlp.NGrams;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TestNGrams {
  @Test
  public void simple() {
    List<List<String>> ngrams = ngramList(1, 1, "hello", "world");
    System.out.println(ngrams);
    assertEquals(asList("hello"), ngrams.get(0));
    assertEquals(asList("world"), ngrams.get(1));
    assertEquals(2, ngrams.size());

    ngrams = ngramList(2, 3, "hello", "n-gram", "world");
    assertEquals(asList(
      asList("hello", "n-gram"), asList("hello", "n-gram", "world"), asList("n-gram", "world")),
      ngrams);

    List<List<String>> ngrams2_4 = ngramList(2, 4, "hello", "n-gram", "world");
    assertEquals(ngrams, ngrams2_4);

    ngrams = ngramList(4, 5, "hello");
    assertEquals(0, ngrams.size());
  }

  @Test
  public void emptyInput() {
    assertEquals(0, ngramList(2, 4).size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void zero() {
    ngramList(0, 0, 1, 2, 3);
  }

  private static <T> List<List<T>> ngramList(int minN, int maxN, T... items) {
    return NGrams.generate(minN, maxN, asList(items)).collect(Collectors.toList());
  }
}

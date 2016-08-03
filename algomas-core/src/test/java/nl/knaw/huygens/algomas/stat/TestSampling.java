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

import org.junit.Test;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.junit.Assert.*;

public class TestSampling {
  @Test
  public void withoutReplacement() {
    int[] sample = Sampling.withoutReplacement(10, 4, i -> i - 1);
    assertArrayEquals(new int[]{6, 7, 8, 9}, sample);

    // Check that the sampling algorithm stops when it has the desired number of elements.
    AtomicInteger nCalls = new AtomicInteger();
    sample = Sampling.withoutReplacement(10, 4, i -> {
      nCalls.incrementAndGet();
      return 0;
    });
    assertArrayEquals(new int[]{0, 1, 2, 3}, sample);
    assertEquals(4, nCalls.get());

    final int POPULATION_SIZE = 100;
    final int ROUNDS = 9999;
    final int SAMPLE_SIZE = 28;

    int[] distribution = new int[POPULATION_SIZE];

    for (int seed = 0; seed < ROUNDS; seed++) {
      sample = Sampling.withoutReplacement(POPULATION_SIZE, SAMPLE_SIZE, new Random(seed));

      assertEquals(sample.length, SAMPLE_SIZE);
      assertEquals(SAMPLE_SIZE,
        stream(sample).boxed().collect(Collectors.toSet()).size());

      stream(sample).forEach(x -> {
        assertTrue(x >= 0);
        assertTrue(x < POPULATION_SIZE);
        distribution[x]++;
      });
    }

    // Check that the distribution that we get is close enough to uniform.
    DoubleSummaryStatistics stats = stream(distribution)
      .mapToDouble(x -> (x / (float) (ROUNDS * SAMPLE_SIZE))).summaryStatistics();
    assertTrue(stats.getMax() < 1.03 * stats.getAverage());
    assertTrue(stats.getMin() > 0.97 * stats.getAverage());
  }

  @Test
  public void iterator() {
    List<String> strings = asList("foo", "bar", "baz", "quux", "null");
    for (int seed : new int[]{1, 2, 3, 4, 5}) {
      assertEquals(strings,
        newArrayList(Sampling.withoutReplacement(strings, strings.size(), new Random(seed))));

      Object[] fromIntegers = Arrays.stream(
        Sampling.withoutReplacement(strings.size(), 3, new Random(seed)))
        .mapToObj(strings::get).toArray();

      Object[] fromIterator = newArrayList(
        Sampling.withoutReplacement(strings, 3, new Random(seed)))
        .toArray();
      assertArrayEquals(fromIntegers, fromIterator);
    }
  }
}

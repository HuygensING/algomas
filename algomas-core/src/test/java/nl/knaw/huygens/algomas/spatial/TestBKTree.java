package nl.knaw.huygens.algomas.spatial;

/*
 * #%L
 * algomas-core
 * %%
 * Copyright (C) 2018 KNAW
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

import nl.knaw.huygens.algomas.nlp.Levenshtein;
import nl.knaw.huygens.algomas.nlp.LevenshteinDamerau;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestBKTree extends BaseTestSpatialTree {
  private static final int[] SEEDS = {1, 17, 19, 24};

  // String metric that counts the number of calls made to it.
  private static class CountingMetric implements IntMetric<String> {
    AtomicInteger calls = new AtomicInteger();
    IntMetric<String> metric;

    CountingMetric(IntMetric<String> metric) {
      super();
      this.metric = metric;
    }

    public int distance(String a, String b) {
      calls.incrementAndGet();
      return metric.distance(a, b);
    }
  }

  @Test
  public void damerau() {
    stringMetric(LevenshteinDamerau::distance, .37);
  }

  @Test
  public void levenshtein() {
    stringMetric(Levenshtein::distance, .36);
  }

  private void stringMetric(IntMetric<String> baseMetric, double savingFactor) {
    long totalCalls = 0;

    for (int seed : SEEDS) {
      ArrayList<String> words = new ArrayList<>(WORDS);
      Collections.shuffle(words, new Random(seed));
      CountingMetric metric = new CountingMetric(baseMetric);
      BKTree<String> tree = new BKTree<>(metric, words);

      assertEquals(WORDS.size(), tree.size());
      assertEquals(WORDS.size(),
        tree.stream().collect(Collectors.toSet()).size());
      assertEquals(new HashSet<>(WORDS), tree.stream().collect(Collectors.toSet()));

      BKTree.Entry<String> nearest = tree
        .nearestNeighbors(1, "ImmutableSet<String>").findFirst()
        .get();
      assertEquals("ImmutableList<String>", nearest.point);

      metric.calls.set(0);

      final int nNeighbors = 10;
      QUERY_WORDS.forEach(w -> {
        List<BKTree.Entry<String>> nn = tree
          .nearestNeighbors(nNeighbors, w)
          .sorted((p, q) -> Double.compare(p.distance,
            q.distance))
          .collect(Collectors.toList());
        assertEquals(nNeighbors, nn.size());

        assertEquals(w, nn.get(0).point);
        assertEquals(0, nn.get(0).distance, 0);
        nn.forEach(entry ->
          assertEquals(0, baseMetric.distance(w, entry.point) - entry.distance));
      });
      totalCalls += metric.calls.get();
    }

    assertTrue(totalCalls <=
      (1 - savingFactor) * WORDS.size() * QUERY_WORDS.size() * SEEDS.length);
  }

  @Test
  public void empty() {
    BKTree<Integer> tree = new BKTree<>((x, y) -> (int) Math.abs(x - y), Collections.emptyList());
    assertTrue(tree.isEmpty());
    assertArrayEquals(new int[]{},
      tree.nearestNeighbors(100, 40).mapToInt(e -> e.distance).toArray());
  }

  @Test
  public void stream() {
    BKTree<String> tree = new BKTree<>((a, b) -> Math.abs(a.charAt(0) - b.charAt(0)), WORDS);
    assertEquals(tree.size(), tree.spliterator().estimateSize());
    assertEquals(tree.size(), tree.stream().count());
    assertEquals(new HashSet<>(WORDS), tree.stream().collect(Collectors.toSet()));
    assertEquals(new HashSet<>(WORDS), tree.stream().parallel().collect(Collectors.toSet()));
  }
}

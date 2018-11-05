package nl.knaw.huygens.algomas.spatial;

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

import nl.knaw.huygens.algomas.nlp.Levenshtein;
import nl.knaw.huygens.algomas.nlp.LevenshteinDamerau;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestVPTree extends BaseTestSpatialTree {
  private static final int[] SEEDS = {1, 17, 19, 24};

  // String metric that counts the number of calls made to it.
  private static class CountingMetric implements Metric<String> {
    AtomicInteger calls = new AtomicInteger();
    Metric<String> metric;

    CountingMetric(Metric<String> metric) {
      super();
      this.metric = metric;
    }

    public double distance(String a, String b) {
      calls.incrementAndGet();
      return metric.distance(a, b);
    }
  }

  @Test
  public void damerau() {
    stringMetric(LevenshteinDamerau::distance, .47);
  }

  @Test
  public void levenshtein() {
    stringMetric(Levenshtein::distance, .46);
  }

  private void stringMetric(Metric<String> baseMetric, double savingFactor) {
    long totalCalls = 0;

    for (int seed : SEEDS) {
      CountingMetric metric = new CountingMetric(baseMetric);
      VPTree<String> tree = new VPTree<>(metric, WORDS, new SplittableRandom(seed));

      assertEquals(WORDS.size(), tree.size());
      assertEquals(WORDS.size(),
        tree.stream().collect(Collectors.toSet()).size());
      assertEquals(new HashSet<>(WORDS),
        tree.stream().collect(Collectors.toSet()));

      VPTree.Entry<String> nearest = tree
        .nearestNeighbors(1, "ImmutableSet<String>").findFirst()
        .get();
      assertEquals("ImmutableList<String>", nearest.point);

      metric.calls.set(0);

      final int nNeighbors = 10;
      QUERY_WORDS.forEach(w -> {
        List<VPTree.Entry<String>> nn = tree
          .nearestNeighbors(nNeighbors, w)
          .sorted((p, q) -> Double.compare(p.distance,
            q.distance))
          .collect(Collectors.toList());
        assertEquals(nNeighbors, nn.size());

        assertEquals(w, nn.get(0).point);
        assertEquals(0, nn.get(0).distance, 0);
        nn.forEach(entry ->
          assertEquals(0, (int) (baseMetric.distance(w, entry.point) - entry.distance)));
      });
      totalCalls += metric.calls.get();
    }

    assertTrue(totalCalls <=
      (1 - savingFactor) * WORDS.size() * QUERY_WORDS.size() * SEEDS.length);
  }

  @Test
  public void levenshteinPredicate() {
    Collection<String> words = asList("foo", "fool", "bar", "bark", "quuxly", "long string", "very long string");
    VPTree<String> tree = new VPTree<>(Levenshtein::distance, words, new SplittableRandom(555));
    List<String> nearest1 = tree.nearestNeighbors(4, "foo")
                                .map(e -> e.point)
                                .filter(s -> !"foo".equals(s))
                                .sorted()
                                .collect(Collectors.toList());
    assertEquals(3, nearest1.size());
    List<String> nearest2 = tree.nearestNeighbors(3, "foo", s -> !"foo".equals(s))
                                .map(e -> e.point)
                                .sorted()
                                .collect(Collectors.toList());
    assertEquals(nearest1, nearest2);
  }

  @Test
  public void serializable() throws Exception {
    Random rnd = new Random(0xb000);
    List<Point2D> points = Stream.generate(() ->
      new Point2D.Double(rnd.nextGaussian(), rnd.nextGaussian()))
                                 .limit(100).collect(Collectors.toList());
    VPTree<Point2D> tree = new VPTree<>(
      (Metric<Point2D> & Serializable) Point2D::distance, points);

    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    new ObjectOutputStream(buf).writeObject(tree);
    //noinspection unchecked
    VPTree<Point2D> reconstructed = (VPTree<Point2D>) new ObjectInputStream(
      new ByteArrayInputStream(buf.toByteArray())).readObject();
    assertEquals(tree.size(), reconstructed.size());
    assertEquals(tree.stream().collect(Collectors.toSet()),
      reconstructed.stream().collect(Collectors.toSet()));
  }

  @Test
  public void radiusQuery() {
    Random rnd = new Random(0xfeefee);
    List<Point2D> points = Stream.generate(() ->
      new Point2D.Double(rnd.nextGaussian(), rnd.nextGaussian()))
                                 .limit(1000).collect(Collectors.toList());
    VPTree<Point2D> tree = new VPTree<>(Point2D::distance, points,
      new SplittableRandom(1));

    Point2D query = new Point2D.Double(rnd.nextGaussian(), rnd.nextGaussian());
    List<VPTree.Entry<Point2D>> result = tree.withinRadius(query, .3)
                                             .collect(Collectors.toList());
    Set<Point2D> nearbyPoints = result.stream().map(e -> e.point)
                                      .collect(Collectors.toSet());

    result.stream().forEach(entry -> {
      assertTrue(nearbyPoints.contains(entry.point) != entry.point.distance(query) > .3);
    });
  }

  @Test
  public void empty() {
    VPTree<Double> tree = new VPTree<>((x, y) -> Math.abs(x - y), Collections.emptyList());
    assertTrue(tree.isEmpty());
    assertArrayEquals(new double[]{},
      tree.nearestNeighbors(100, Math.PI).mapToDouble(e -> e.distance).toArray(), 0);
  }

  @Test
  public void stream() {
    VPTree<String> tree = new VPTree<>((a, b) -> Math.abs(a.charAt(0) - b.charAt(0)), WORDS);
    assertEquals(tree.size(), tree.spliterator().estimateSize());
    assertEquals(tree.size(), tree.stream().count());
    assertEquals(new HashSet<>(WORDS), tree.stream().collect(Collectors.toSet()));
    assertEquals(new HashSet<>(WORDS), tree.stream().parallel().collect(Collectors.toSet()));
  }

  @Test
  public void distance0() {
    List<String> keys = asList("foo", "foo", "foo", "bar", "bar", "baz");
    VPTree<String> tree = new VPTree<>(Levenshtein::distance, keys);
    assertEquals(singletonList("foo"),
      tree.nearestNeighbors(1, 0, "foo")
          .map(entry -> entry.point)
          .collect(Collectors.toList())
    );
    assertEquals(keys.subList(0, 3),
      tree.nearestNeighbors(3, 0, "foo")
          .map(entry -> entry.point)
          .collect(Collectors.toList())
    );
    assertEquals(keys.subList(0, 3),
      tree.withinRadius("foo", 0)
          .map(entry -> entry.point)
          .collect(Collectors.toList())
    );
  }
}

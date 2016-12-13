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
import java.util.ArrayList;
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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestVPTree {
  private static final int[] SEEDS = {1, 17, 19, 24};

  private static final List<String> QUERY_WORDS = asList("static",
    "final", "ImmutableList<String>", "String", "Levenshtein",
    "Damerau", "Wagner", "Fischer", "Kruskal", "Wallis", "XYZZYFLUX",
    "tree", "distance", "public", "private", "AtomicInteger", "Assert",
    "filter", "map", "expected", "size", "words", "void", "BKTree",
    "DamerauLevenshtein", "assertEquals", "concurrent", "atomic",
    "class", "Java", "Builder", "Guava", "Apache", "Commons-lang",
    "Python", "C", "C++", "Groovy", "Jython", "John Doe", "Jane Doe",
    "Billybob", "ampersand", "edit distance", "VP-tree", "indel cost",
    "transposition", "macromolecule", "time warping", "0123456789");

  private static final List<String> words = new ArrayList<>();

  static {
    words.addAll(QUERY_WORDS);
    QUERY_WORDS.stream()
               .flatMap(x -> QUERY_WORDS.stream().map(y -> x + " -- " + y))
               .forEachOrdered(words::add);
    words.addAll(asList("foo", "bar", "baz", "quux"));
  }

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
      VPTree<String> tree = new VPTree<String>(metric, words,
        new SplittableRandom(seed));

      assertEquals(words.size(), tree.size());
      assertEquals(words.size(),
        tree.stream().collect(Collectors.toSet()).size());
      assertEquals(new HashSet<String>(words),
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
      (1 - savingFactor) * words.size() * QUERY_WORDS.size() * SEEDS.length);
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
    VPTree<String> tree = new VPTree<>((a, b) -> Math.abs(a.charAt(0) - b.charAt(0)), words);
    assertEquals(new HashSet<>(words), tree.stream().collect(Collectors.toSet()));
    assertEquals(new HashSet<>(words), tree.stream().parallel().collect(Collectors.toSet()));
  }
}

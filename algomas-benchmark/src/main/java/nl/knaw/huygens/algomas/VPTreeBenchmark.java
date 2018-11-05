package nl.knaw.huygens.algomas;

/*
 * #%L
 * JMH benchmarks for algomas-core
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
import nl.knaw.huygens.algomas.spatial.VPTree;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 1)
public class VPTreeBenchmark {
  // Base list of words.
  private static final List<String> WORDS = asList("static",
    "final", "ImmutableList<String>", "String", "Levenshtein",
    "Damerau", "Wagner", "Fischer", "Kruskal", "Wallis", "XYZZYFLUX",
    "tree", "distance", "public", "private", "AtomicInteger", "Assert",
    "filter", "map", "expected", "size", "words", "void", "BKTree",
    "DamerauLevenshtein", "assertEquals", "concurrent", "atomic",
    "class", "Java", "Builder", "Guava", "Apache", "Commons-lang",
    "Python", "C", "C++", "Groovy", "Jython", "John Doe", "Jane Doe",
    "Billybob", "ampersand", "edit distance", "VP-tree", "indel cost",
    "transposition", "macromolecule", "time warping", "0123456789");

  // List of query words, filled below.
  private static final List<String> QUERIES = new ArrayList<>();

  // List of words to be indexed in VP-tree, filled below.
  private static final List<String> TO_INDEX = new ArrayList<>();

  // Return n randomly corrupted variants of s.
  private static Stream<String> corrupt(String s, Random rnd, int n) {
    return rnd.ints(n, 0, s.length())
              .mapToObj(i -> {
                char letter = "abcdefghijklmnopqrstuvwxyz".charAt(rnd.nextInt(26));
                return s.substring(0, i) + letter + s.substring(i);
              });
  }

  static {
    Random rnd = new Random(0x4aee62);
    WORDS.stream().flatMap(w -> corrupt(w, rnd, 100))
         .forEach(TO_INDEX::add);
    WORDS.stream().flatMap(w -> corrupt(w, rnd, 3))
         .forEach(QUERIES::add);
  }

  private static VPTree<String> makeTree() {
    return new VPTree<>(Levenshtein::distance, TO_INDEX);
  }

  @Benchmark
  public static VPTree<String> construct() {
    return makeTree();
  }

  private static final VPTree<String> tree = construct();

  @Benchmark
  public static double neighbors3() {
    return QUERIES.stream().flatMapToDouble(q ->
      tree.nearestNeighbors(3, q).mapToDouble(e -> e.distance))
                  .average().getAsDouble();
  }

  @Benchmark
  public static double neighbors3Brute() {
    return QUERIES
      .stream()
      .flatMapToDouble(q -> {
        List<Integer> distances = TO_INDEX.stream().map(w -> Levenshtein.distance(w, q))
                                          .collect(Collectors.toList());
        Sort.partial(distances, 3);
        return distances.subList(0, 3).stream().mapToDouble(x -> (double) x);
      })
      .average().getAsDouble();
  }

  @Benchmark
  public static int iterator() {
    int n = 0;
    for (String ignored : tree) {
      n++;
    }
    return n;
  }

  private static ToDoubleFunction<String> function = s -> {
    int vowels = 0;
    s = s.toLowerCase();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if ("aeiouy".indexOf(c) != -1) {
        vowels++;
      }
    }
    return vowels / (double) s.length();
  };

  @Benchmark
  public static double stream() {
    return tree.stream().parallel()
               .mapToDouble(function).filter(Double::isFinite).sum();
  }
}

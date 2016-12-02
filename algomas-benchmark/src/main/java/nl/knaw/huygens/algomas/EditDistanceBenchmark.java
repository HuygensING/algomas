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

import com.google.common.primitives.Chars;
import nl.knaw.huygens.algomas.editdist.AbstractLevenshtein;
import nl.knaw.huygens.algomas.editdist.GenericLevenshtein;
import nl.knaw.huygens.algomas.nlp.Levenshtein;
import nl.knaw.huygens.algomas.nlp.LevenshteinDamerau;
import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(value = 1)
public class EditDistanceBenchmark {
  private static final String[] words = new String[]{
    "january", "february", "march", "april", "may", "june", "july", "august",
    "september", "october", "november", "december", "4th of july", "march of folly",
    "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
    "foo", "bar", "baz", "quux", "Fred", "Barney", "Wilma", "float", "int",
    "static", "final", "ImmutableList", "String", "Levenshtein", "Damerau",
    "tree", "distance", "public", "private", "AtomicInteger", "Assert", "filter",
    "map", "expected", "size", "words", "void", "BKTree", "LevenshteinDamerau",
    "assertEquals", "concurrent", "atomic", "class", "Java", "Builder", "Guava",
    "Apache", "Commons-lang", "Python", "C", "C++", "Groovy", "Jython",
    "ImmutableList<String>", "Apache Commons", "integer", "double precision",
    "floating point",
  };

  private static final List<List<Character>> lists =
    Arrays.asList(words).stream()
          .map(w -> Chars.asList(w.toCharArray()))
          .collect(Collectors.toList());

  private interface ListMetric {
    int distance(List<Character> a, List<Character> b);
  }

  private static double pairwiseLists(ListMetric metric) {
    double total = 0;
    for (int i = 0; i < lists.size(); i++) {
      for (int j = i; j < lists.size(); j++) {
        total += metric.distance(lists.get(i), lists.get(j));
      }
    }
    return total;
  }

  private interface StringMetric {
    int distance(String a, String b);
  }

  private static double pairwiseStrings(StringMetric metric) {
    double total = 0;
    for (int i = 0; i < words.length; i++) {
      for (int j = i; j < words.length; j++) {
        total += metric.distance(words[i], words[j]);
      }
    }
    return total;
  }

  @Benchmark
  public double abstractLevenshtein() {
    AbstractLevenshtein<Character> levenshtein = new AbstractLevenshtein<Character>() {
      @Override
      protected int indelCost(Character x) {
        return 1;
      }

      @Override
      protected int substCost(Character x, Character y) {
        return 1;
      }
    };

    return pairwiseLists(levenshtein::distance);
  }

  @Benchmark
  public double bounded3() {
    return pairwiseStrings((a, b) -> Levenshtein.boundedDistance(a, b, 3));
  }

  @Benchmark
  public double genericLevenshtein() {
    return pairwiseLists(new GenericLevenshtein()::distance);
  }

  @Benchmark
  public double levenshtein() {
    return pairwiseStrings(Levenshtein::distance);
  }

  @Benchmark
  public double levenshteinDamerau() {
    return pairwiseStrings(LevenshteinDamerau::distance);
  }

  // As of Apache Commons-Lang 3.4, their Levenshtein implementation
  // lacks three of the optimizations that we have: skipping common
  // prefixes, skipping common suffixes, and storing a single row of
  // the DP matrix (they store two).
  @Benchmark
  public double commons() {
    return pairwiseStrings(StringUtils::getLevenshteinDistance);
  }

  @Benchmark
  public double commonsBounded3() {
    return pairwiseStrings((a, b) -> {
      int d = StringUtils.getLevenshteinDistance(a, b, 3);
      if (d == -1) {
        d = 4;
      }
      return d;
    });
  }
}

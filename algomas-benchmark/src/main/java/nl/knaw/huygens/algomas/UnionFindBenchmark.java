package nl.knaw.huygens.algomas;

/*
 * #%L
 * JMH benchmarks for algomas-core
 * %%
 * Copyright (C) 2018 Huygens ING (KNAW)
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

import nl.knaw.huygens.algomas.graph.UnionFind;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;

@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(value = 1)
public class UnionFindBenchmark {
  private static final int NPAIRS = 10000;
  private static final int[] first = new int[NPAIRS];
  private static final int[] second = new int[NPAIRS];

  static {
    Random rnd = new Random(3613);
    for (int i = 0; i < NPAIRS; i++) {
      first[i] = rnd.nextInt(NPAIRS);
      do {
        second[i] = rnd.nextInt(NPAIRS);
      } while (second[i] == first[i]);
    }
  }

  @Benchmark
  public int small() {
    return benchmark(10000, 1000);
  }

  @Benchmark
  public int medium() {
    return benchmark(100000, 5000);
  }

  @Benchmark
  public int large() {
    return benchmark(10000000, NPAIRS);
  }

  private static int benchmark(int n, int npairs) {
    UnionFind uf = new UnionFind(n);
    for (int i = 0; i < npairs; i++) {
      uf.union(first[i], second[i]);
    }
    return countSets(uf);
  }

  private static int countSets(UnionFind uf) {
    int nsets = 0;
    for (int i = 0; i < uf.size(); i++) {
      if (uf.find(i) == i) {
        nsets++;
      }
    }
    return nsets;
  }
}

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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.*;

@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 3)
public class PartialSortBenchmark {
  private static final List<Integer> integers = new ArrayList<>();

  static {
    Random rnd = new Random(16);
    for (int i = 0; i < 1000000; i++) {
      integers.add(rnd.nextInt());
    }
  }

  private List<Integer> copyData() {
    return Arrays.asList(integers.toArray(new Integer[0]));
  }

  @Benchmark
  public List<Integer> partialSortIntegers() {
    List<Integer> list = copyData();
    Sort.partial(list, 100);
    return list;
  }

  @Benchmark
  public List<Integer> selectIntegers() {
    List<Integer> list = copyData();
    Sort.select(list, 100);
    return list;
  }

  @Benchmark
  public List<Integer> sortIntegers() {
    List<Integer> list = copyData();
    Collections.sort(list);
    return list;
  }
}

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

import nl.knaw.huygens.algomas.nlp.NGrams;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 1)
public class NGramsBenchmark {
  private static final List<Integer> inputs = new ArrayList<>();

  static {
    for (int i = 0; i < 100000; i++) {
      inputs.add(i);
    }
  }

  @Benchmark
  public List<List<Integer>> smallN() {
    return NGrams.generate(2, 5, inputs).parallel()
      .collect(Collectors.toList());
  }

  @Benchmark
  public List<List<Integer>> largeN() {
    return NGrams.generate(4, 11, inputs).parallel()
      .collect(Collectors.toList());
  }
}

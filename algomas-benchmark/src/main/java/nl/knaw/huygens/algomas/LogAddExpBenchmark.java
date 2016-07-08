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

import org.apache.commons.math3.util.FastMath;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;

@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 3)
public class LogAddExpBenchmark {
  private static final double[] values = new double[10000];

  static {
    Random rnd = new Random(27182818);
    for (int i = 0; i < values.length; i++) {
      values[i] = 1 - rnd.nextDouble();
    }
  }

  // Alternative implementation of logAddExp. If this turns out to be faster,
  // consider switching the implementations. Compared to OpenJDK 7 on a
  // Sandy Bridge, it's 25% slower.
  private static double logAddExp(double x, double y) {
    if (x < y) {
      double t = x;
      x = y;
      y = t;
    }
    return x + FastMath.log1p(FastMath.exp(y - x));
  }

  @Benchmark
  public static double benchmarkLogAddExp() {
    double total = Math.log(.5);
    for (double x : values) {
      total = ExtMath.logAddExp(total, x);
    }
    return total;
  }

  @Benchmark
  public static double benchmarkLogAddExpFastMath() {
    double total = Math.log(.5);
    for (double x : values) {
      total = logAddExp(total, x);
    }
    return total;
  }
}

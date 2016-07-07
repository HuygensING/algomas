package nl.knaw.huygens.algomas;

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
    for (int i = 0; i < values.length; i++) {
      total = ExtMath.logAddExp(total, values[i]);
    }
    return total;
  }

  @Benchmark
  public static double benchmarkLogAddExpFastMath() {
    double total = Math.log(.5);
    for (int i = 0; i < values.length; i++) {
      total = logAddExp(total, values[i]);
    }
    return total;
  }
}

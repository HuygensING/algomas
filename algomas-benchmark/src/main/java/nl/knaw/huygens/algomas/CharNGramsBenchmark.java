package nl.knaw.huygens.algomas;

import nl.knaw.huygens.algomas.nlp.NGrams;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 1)
public class CharNGramsBenchmark {
  private static String data;

  static {
    data = "";
    for (int i = 0; i < 100; i++) {
      data += "foobarbla56218561298";
    }
  }

  @Benchmark
  public double generateCharNGrams() {
    return NGrams.ofChars(2, 8, data)
      .mapToInt(CharSequence::length).average().getAsDouble();
  }
}

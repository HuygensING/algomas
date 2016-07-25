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

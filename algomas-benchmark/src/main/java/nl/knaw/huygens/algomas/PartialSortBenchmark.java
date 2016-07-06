package nl.knaw.huygens.algomas;

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

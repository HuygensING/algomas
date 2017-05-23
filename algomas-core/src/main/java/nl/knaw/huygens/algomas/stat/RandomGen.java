package nl.knaw.huygens.algomas.stat;

import java.util.List;
import java.util.function.IntUnaryOperator;

public class RandomGen {
  private RandomGen() {
  }

  /**
   * Replacement for {@link java.util.Collections#shuffle}.
   * <p>
   * Works with {@link java.util.SplittableRandom} as well as {@link java.util.Random}
   * and other sources of randomness.
   *
   * @param list    List to shuffle.
   * @param nextInt Source of random numbers, e.g., {@link java.util.Random#nextInt(int)}.
   */
  public static void shuffle(List list, IntUnaryOperator nextInt) {
    // Fisher-Yates shuffle.
    for (int i = list.size(); --i > 0; ) {
      int r = nextInt.applyAsInt(i + 1);
      list.set(i, list.set(r, list.get(i)));
    }
  }
}

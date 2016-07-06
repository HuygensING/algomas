package nl.knaw.huygens.algomas;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.ToDoubleFunction;

/**
 * Functional programming utilities.
 */
public class Functional {
  /**
   * Turn an Iterator into an Iterable.
   * <p>
   * This supports the idiom
   * <pre>
   *     for (T x : in(iterator)) { process(x); }
   * </pre>
   *
   * @param iter
   * @param <T>
   * @return
   */
  public static <T> Iterable<T> in(Iterator<T> iter) {
    return () -> iter;
  }
}

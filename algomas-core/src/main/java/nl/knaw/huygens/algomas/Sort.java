package nl.knaw.huygens.algomas;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sorting algorithms.
 */
public class Sort {
  /**
   * Partial sorting by natural order.
   */
  public static <T extends Comparable<? super T>> void partial(List<T> l, int k) {
    Comparator<T> comp = Comparator.naturalOrder();
    partial(l, comp, k);
  }

  /**
   * Partially sort the list l, so that the first k elements are the k
   * smallest, in order according to comp.
   * <p>
   * The code
   * <pre>
   *
   * </pre>
   */
  public static <T> void partial(List<T> l, Comparator<T> comp, int k) {
    int n = l.size();
    k = Math.min(k, n);
    int maxDepth = 2 * (int) Math.log(n + 1);
    partial(l, comp, k, 0, n, maxDepth);
  }

  // Cutoff for switching to selection sort in partial quicksort.
  // Needs to be at least 3 for median-of-three partitioning to be sensible.
  private static final int SELSORT_CUTOFF = 5;

  // This is C. Martínez's partial quicksort algorithm (http://www.cs.upc.edu/~conrado/research/talks/aofa04.pdf,
  // http://www.siam.org/meetings/analco04/abstracts/CMartinez.pdf), amended with an introsort-style recursion
  // depth bound and switch to ordinary sorting to prevent quadratic worst-case time complexity.
  private static <T> void partial(List<T> a, Comparator<T> comp, int k, int lo, int hi, int maxDepth) {
    while (hi - lo > SELSORT_CUTOFF && maxDepth-- > 0) {
      int pivotIndex = medianOfThree(a, comp, lo, hi);
      pivotIndex = partition(a, comp, lo, hi, pivotIndex);
      if (pivotIndex < k - 1) {
        partial(a, comp, k, pivotIndex + 1, hi, maxDepth);
      }
      hi = pivotIndex;
    }

    if (maxDepth == 0) {
      Collections.sort(a.subList(lo, hi), comp);
      return;
    }

    k = Math.min(k, hi - lo);
    // Selection sort.
    for (; k > 0; k--, lo++) {
      T min = a.get(lo);
      int minIndex = lo;
      for (int i = lo + 1; i < hi; i++) {
        T x = a.get(i);
        if (comp.compare(x, min) < 0) {
          min = x;
          minIndex = i;
        }
      }
      swap(a, lo, minIndex);
    }
  }

  /**
   * Selection by natural order.
   */
  public static <T extends Comparable<? super T>> void select(List<T> a, int k) {
    Comparator<T> comp = Comparator.naturalOrder();
    select(a, comp, k);
  }

  /**
   * Partially sort the list l, so that the first k elements are the k
   * smallest.
   * <p>
   * Unlike partial, this does not sort the first k elements. Its main purpose
   * is to put the k'th smallest element is in position k.
   * <p>
   * For example, to find the median of a list of odd size, do
   * <p>
   * <pre>
   *     select(list, comp, list.size() / 2)
   *     T median = list.get(list.size() / 2);
   * </pre>
   */
  public static <T> void select(List<T> l, Comparator<T> comp, int k) {
    for (int lo = 0, hi = l.size(); hi - lo > 1; ) {
      int pivotIndex = medianOfThree(l, comp, lo, hi);
      pivotIndex = partition(l, comp, lo, hi, pivotIndex);
      if (pivotIndex == k) {
        return;
      } else if (k < pivotIndex) {
        hi = pivotIndex;
      } else {
        lo = pivotIndex + 1;
      }
    }
  }

  private static <T> void compareAndSwap(List<T> a, Comparator<T> comp, int i, int j) {
    final T x = a.get(i);
    final T y = a.get(j);
    if (comp.compare(x, y) > 0) {
      a.set(i, y);
      a.set(j, x);
    }
  }

  private static <T> int medianOfThree(List<T> a, Comparator<T> comp, int lo,
                                       int hi) {
    int mid = lo + (hi - lo) / 2;
    hi--;

    compareAndSwap(a, comp, lo, mid);
    compareAndSwap(a, comp, mid, hi);
    compareAndSwap(a, comp, lo, hi);

    return mid;
  }

  // Partition function from Bentley's Programming Pearls (qsort3, page 120).
  private static <T> int partition(List<T> a, Comparator<T> comp, int lo,
                                   int hi, int pivotIndex) {
    T pivot = a.get(pivotIndex);
    a.set(pivotIndex, a.get(lo));
    a.set(lo, pivot);
    int i = lo, j = hi;

    for (; ; ) {
      do {
        i++;
      } while (i < hi && comp.compare(a.get(i), pivot) < 0);
      do {
        j--;
      } while (comp.compare(a.get(j), pivot) > 0);
      if (i >= j) {
        break;
      }
      swap(a, i, j);
    }
    swap(a, lo, j);
    return j;
  }

  private static <T> void swap(List<T> a, int i, int j) {
    a.set(i, a.set(j, a.get(i)));
  }
}

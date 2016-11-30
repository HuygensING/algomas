package nl.knaw.huygens.algomas.editdist;

import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for generic edit distances.
 * <p>
 * Subclasses must implement indelCost, which measures the cost of inserting
 * or deleting a particular symbol, and substCost, which measures the cost of
 * substituting a symbol x for a symbol y. The latter must be a symmetric
 * function: substCost(x,y) == substCost(y,x) for all x, y. It is assumed that
 * substCost(x,y) == 0 whenever Objects.equals(x,y).
 *
 * @param <T> Element type in the sequences that are compared.
 */
public abstract class AbstractLevenshtein<T> {
  protected abstract int indelCost(T x);

  protected abstract int substCost(T x, T y);

  public final int distance(List<T> a, List<T> b) {
    int m = a.size();
    int n = b.size();

    if (m > n) {
      // Make sure a is the shorter string, since its length determines
      // how much memory we use.
      int t = n;
      n = m;
      m = t;
      List<T> l = a;
      a = b;
      b = l;
    }

    int commonPrefix = commonPrefix(a, b);
    if (commonPrefix > 0) {
      a = a.subList(commonPrefix, m);
      b = b.subList(commonPrefix, n);
    }
    int commonSuffix = commonSuffix(a, b);
    if (commonSuffix > 0) {
      a = a.subList(0, a.size() - commonSuffix);
      b = b.subList(0, b.size() - commonSuffix);
    }
    m = a.size();
    n = b.size();

    if (m == 0) {
      return b.stream().mapToInt(this::indelCost).sum();
    }

    int[] table = new int[m + 1];

    table[0] = 0;
    for (int i = 1; i <= m; i++) {
      table[i] = table[i - 1] + indelCost(a.get(i - 1));
    }
    for (int j = 1; j <= n; j++) {
      int prevDiag = table[0];
      T bj = b.get(j - 1);
      table[0] += indelCost(bj);

      for (int i = 1; i <= m; i++) {
        int currDiag = table[i];
        T ai = a.get(i - 1);
        if (Objects.equals(ai, bj)) {
          table[i] = prevDiag;
        } else {
          table[i] = min(
            add(table[i - 1], indelCost(ai)),
            add(table[i], indelCost(bj)),
            add(prevDiag, substCost(ai, bj)));
        }
        prevDiag = currDiag;
      }
    }
    return table[m];
  }

  private static int add(int a, int b) {
    int sum = a + b;
    return sum < 0 ? Integer.MAX_VALUE : sum;
  }

  private static <T> int commonPrefix(List<T> a, List<T> b) {
    int n = Math.min(a.size(), b.size());
    for (int i = 0; i < n; i++) {
      if (!Objects.equals(a.get(i), b.get(i))) {
        return i;
      }
    }
    return n;
  }

  private static <T> int commonSuffix(List<T> a, List<T> b) {
    int m = a.size();
    int n = b.size();
    int min = Math.min(m, n);
    for (int i = 0; i < min; i++) {
      if (!Objects.equals(a.get(m - i - 1), b.get(n - i - 1))) {
        return i;
      }
    }
    return min;
  }

  private static int min(int a, int b, int c) {
    return Math.min(Math.min(a, b), c);
  }
}

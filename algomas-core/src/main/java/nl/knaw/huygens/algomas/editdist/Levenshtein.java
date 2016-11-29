package nl.knaw.huygens.algomas.editdist;

import java.util.List;
import java.util.Objects;

/**
 * Generic edit distance.
 * <p>
 * Unlike the Levenshtein and LevenshteinDamerau classes in package nlp,
 * instances of this class are parameterized on the input type and on
 * atomic edit operation costs.
 */
public class Levenshtein<T> {
  public final int indelCost;
  public final int substCost;

  public Levenshtein() {
    this(1, 1);
  }

  public Levenshtein(int indelCost, int substCost) {
    this.indelCost = indelCost;
    this.substCost = substCost;
  }

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
      return n * indelCost;
    }

    int[] table = new int[m + 1];

    for (int i = 0; i <= m; i++) {
      table[i] = i * indelCost;
    }
    for (int j = 1; j <= n; j++) {
      table[0] = j * indelCost;
      T bj = b.get(j - 1);

      for (int i = 1, prevDiag = (j - 1) * indelCost; i <= m; i++) {
        int currDiag = table[i];
        if (Objects.equals(a.get(i - 1), bj)) {
          table[i] = prevDiag;
        } else {
          table[i] = min(
            add(table[i - 1], indelCost),
            add(table[i], indelCost),
            add(prevDiag, substCost));
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

package nl.knaw.huygens.algomas.nlp;

/*
 * #%L
 * algomas-core
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

public class Levenshtein {
  /**
   * Returns min(Levenshtein.distance(a, b), maxDist).
   * <p>
   * When maxDist is small, this method is faster than actually computing the
   * distance, then taking the min.
   *
   * @param a
   * @param b
   * @param maxDist
   * @return The Levenshtein distance or maxDist+1, whichever is smaller.
   */
  public static int boundedDistance(String a, String b, int maxDist) {
    int m = a.length();
    int n = b.length();

    if (m > n) {
      // Make sure a is the shorter string, since its length determines
      // how much memory we use.
      int t = n;
      n = m;
      m = t;
      String s = a;
      a = b;
      b = s;
    }

    int commonPrefix = EditDistanceUtil.commonPrefix(a, b);
    int commonSuffix = EditDistanceUtil.commonSuffix(a, b, commonPrefix);

    StringSlice x = new StringSlice(a, commonPrefix, m - commonSuffix);
    StringSlice y = new StringSlice(b, commonPrefix, n - commonSuffix);
    m -= commonPrefix + commonSuffix;
    n -= commonPrefix + commonSuffix;

    if (m == 0) {
      return Math.min(maxDist, n);
    }

    int[] table = new int[m + 1];

    for (int i = 0, bound = Math.min(maxDist, m); i <= bound; i++) {
      table[i] = i;
    }

    for (int j = 1; j <= n; j++) {
      // Start and end of the "band" in the full DP table that we
      // consider.
      int bandStart = Math.max(1, j - maxDist);
      int bandEnd = Math.min(m, (j + maxDist < 0) ? m : j + maxDist);
      if (bandStart > bandEnd) {
        return maxDist;
      }

      table[0] = j;
      char bj = y.charAt(j - 1);
      int prevDiag = j - 1;

      for (int i = bandStart; i <= bandEnd; i++) {
        int currDiag = table[i];
        if (x.charAt(i - 1) == bj) {
          table[i] = prevDiag;
        } else {
          table[i] = min(
            table[i - 1] + 1,
            table[i] + 1,
            prevDiag + 1);
        }
        prevDiag = currDiag;
      }
    }
    // The strange max takes care of the corner case maxDist ==
    // Integer.MAX_VALUE.
    return table[m] > maxDist ? maxDist : table[m];
  }

  /**
   * Returns the Levenshtein distance between a and b.
   * <p>
   * Each edit operation has unit cost.
   *
   * @param a
   * @param b
   * @return
   */
  public static int distance(String a, String b) {
    int m = a.length();
    int n = b.length();

    if (m > n) {
      // Make sure a is the shorter string, since its length determines
      // how much memory we use.
      int t = n;
      n = m;
      m = t;
      String s = a;
      a = b;
      b = s;
    }

    int commonPrefix = EditDistanceUtil.commonPrefix(a, b);
    int commonSuffix = EditDistanceUtil.commonSuffix(a, b, commonPrefix);

    StringSlice x = new StringSlice(a, commonPrefix, m - commonSuffix);
    StringSlice y = new StringSlice(b, commonPrefix, n - commonSuffix);
    m -= commonPrefix + commonSuffix;
    n -= commonPrefix + commonSuffix;

    if (m == 0) {
      return n;
    }

    int[] table = new int[m + 1];

    for (int i = 0; i <= m; i++) {
      table[i] = i;
    }
    for (int j = 1; j <= n; j++) {
      table[0] = j;
      char bj = y.charAt(j - 1);

      for (int i = 1, prevDiag = j - 1; i <= m; i++) {
        int currDiag = table[i];
        if (x.charAt(i - 1) == bj) {
          table[i] = prevDiag;
        } else {
          table[i] = min(
            table[i - 1] + 1,
            table[i] + 1,
            prevDiag + 1);
        }
        prevDiag = currDiag;
      }
    }
    return table[m];
  }

  private static int min(int a, int b, int c) {
    return Math.min(Math.min(a, b), c);
  }
}

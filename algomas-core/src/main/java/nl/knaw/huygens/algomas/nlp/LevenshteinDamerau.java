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

public class LevenshteinDamerau {
  private static class Matrix {
    final int[] entry;
    final int rowLength;

    Matrix(int m, int n) {
      rowLength = n + 2;
      entry = new int[(m + 2) * rowLength];
    }

    final int get(int i, int j) {
      return entry[(i + 1) * rowLength + (j + 1)];
    }

    final void set(int i, int j, int value) {
      entry[(i + 1) * rowLength + (j + 1)] = value;
    }
  }

  // Cost of insertion/deletion, substitution, transposition,
  // currently hardcoded.
  private static final int INDEL = 1;
  private static final int SUBST = 1;
  private static final int TRANSP = 1;

  /**
   * Returns the Levenshtein-Damerau distance between a and b.
   * <p>
   * This is a variant of Levenshtein edit distance where a transposition (swap)
   * of two adjacent symbols is considered a single edit operation.
   * Each edit operation has unit cost.
   */
  public static int distance(String a, String b) {
    int m = a.length();
    int n = b.length();

    int commonPrefix = EditDistanceUtil.commonPrefix(a, b);
    int commonSuffix = EditDistanceUtil.commonSuffix(a, b, commonPrefix);

    StringSlice x = new StringSlice(a, commonPrefix, m - commonSuffix);
    StringSlice y = new StringSlice(b, commonPrefix, n - commonSuffix);
    m -= commonPrefix + commonSuffix;
    n -= commonPrefix + commonSuffix;

    Matrix d = new Matrix(m, n);

    // Algorithm S of Lowrance and Wagner,
    // http://www.lemoda.net/text-fuzzy/lowrance-wagner/lowrance-wagner.pdf.
    // XXX This implementation takes O(n*m) space. It should be possible to do
    // this in linear space using the same trick employed in Levenshtein::distance.
    int maxDist = (m + n) * INDEL + 1;
    d.set(-1, -1, maxDist);
    for (int i = 0; i <= m; i++) {
      d.set(i, -1, maxDist);
      d.set(i, 0, i * INDEL);
    }
    for (int j = 0; j <= n; j++) {
      d.set(-1, j, maxDist);
      d.set(0, j, j * INDEL);
    }

    int maxChar = 0;
    for (int i = 0; i < m; i++) {
      maxChar = Math.max(maxChar, x.charAt(i));
    }
    for (int j = 0; j < n; j++) {
      maxChar = Math.max(maxChar, y.charAt(j));
    }
    int[] da = new int[maxChar + 1];

    for (int i = 1; i <= m; i++) {
      char xi = x.charAt(i - 1);
      int db = 0;

      for (int j = 1; j <= n; j++) {
        char bj = y.charAt(j - 1);
        int i1 = da[bj];
        int j1 = db;

        int substCost = SUBST;
        if (xi == bj) {
          substCost = 0;
          db = j;
        }

        d.set(i, j, min(
          d.get(i - 1, j - 1) + substCost,
          d.get(i, j - 1) + INDEL,
          d.get(i - 1, j) + INDEL,
          d.get(i1 - 1, j1 - 1) + (i - i1 - 1) * INDEL +
            TRANSP + (j - j1 - 1) * INDEL));
      }
      da[xi] = i;
    }

    return d.get(m, n);
  }

  private static int min(int a, int b, int c, int d) {
    return Math.min(Math.min(a, b), Math.min(c, d));
  }
}

package nl.knaw.huygens.algomas.graph;

/*
 * #%L
 * algomas-core
 * %%
 * Copyright (C) 2018 Huygens ING (KNAW)
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

/**
 * Disjoint-set data structure with union and find operations.
 */
public class UnionFind {
  private final int[] parent;
  private final byte[] rank;

  public UnionFind(int n) {
    parent = new int[n];
    rank = new byte[n];
    for (int i = 0; i < n; i++) {
      parent[i] = i;
    }
  }

  /**
   * Find the representative of set x.
   */
  public final int find(int x) {
    // Find with path halving, https://www.cs.princeton.edu/~rs/AlgsDS07/01UnionFind.pdf
    while (x != parent[x]) {
      parent[x] = parent[parent[x]];
      x = parent[x];
    }
    return x;
  }

  /**
   * Returns the size of the union-find structure (the initial number of sets).
   */
  public int size() {
    return parent.length;
  }

  /**
   * Merge the sets containing x and y.
   *
   * @return true iff x and y where previously in disjoint sets.
   */
  public final boolean union(int x, int y) {
    x = find(x);
    y = find(y);
    if (x == y) {
      return false;
    }

    if (rank[x] < rank[y]) {
      parent[x] = y;
    } else if (rank[x] > rank[y]) {
      parent[y] = x;
    } else {
      parent[y] = x;
      rank[y]++;
    }
    return true;
  }
}

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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestUnionFind {
  @Test
  public void smokeTest() {
    final int n = 20;
    UnionFind sets = new UnionFind(n);

    assertEquals(n, sets.size());

    for (int i = 0; i < sets.size(); i++) {
      for (int j = 0; j < sets.size(); j++) {
        if (i == j) {
          assertEquals(sets.find(i), sets.find(j));
        } else {
          assertTrue(sets.find(i) != sets.find(j));
        }
      }
    }

    assertTrue(sets.union(4, 3));
    assertEquals(sets.find(3), sets.find(4));
    assertFalse(sets.union(3, 4));
    assertEquals(sets.find(3), sets.find(4));

    assertTrue(sets.union(1, 3));
    assertEquals(sets.find(1), sets.find(3));
    assertTrue(sets.union(2, 3));
    assertEquals(sets.find(2), sets.find(3));
    assertEquals(sets.find(1), sets.find(4));
    assertFalse(sets.union(4, 1));

    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        sets.union(i, j);
      }
    }
    // There should be one set left now, i.e., one element that is its own
    // representative.
    int nsets = 0;
    for (int i = 0; i < n; i++) {
      if (sets.find(i) == i) {
        nsets++;
      }
    }
    assertEquals(1, nsets);
  }

  // Regression test for faulty union algorithm that broke chains.
  @Test
  public void regression() {
    UnionFind uf = new UnionFind(5);

    int n = 5;
    n -= uf.union(1, 2) ? 1 : 0;
    n -= uf.union(2, 3) ? 1 : 0;
    n -= uf.union(3, 4) ? 1 : 0;
    n -= uf.union(0, 3) ? 1 : 0;

    assertEquals(1, n);

    int repr = uf.find(0);
    for (int i = 1; i < uf.size(); i++) {
      assertEquals(repr, uf.find(1));
    }
  }
}

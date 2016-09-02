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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLevenshteinDamerau {
  private final String[] words = new String[]{"foo", "fool", "food", "bar", "baz",
    "bra", "quux", "quuux", "super", "duper", "supercalifragilisticexpialidocious"};

  @Test
  public void simple() {
    for (String word : new String[]{"bra", "bard", "br", "car"}) {
      assertEquals(1, LevenshteinDamerau.distance("bar", word));
    }
  }

  @Test
  public void compareWithLevenshtein() {
    for (int i = 0; i < words.length; i++) {
      for (int j = i + 1; j < words.length; j++) {
        String a = words[i];
        String b = words[j];
        int dl = LevenshteinDamerau.distance(a, b);
        int l = Levenshtein.distance(a, b);
        // System.out.format("%s %s %d %d\n", strA, strB, dl, l);
        Assert.assertTrue(
          String.format("%d > %d for %s, %s", dl, l, a, b),
          dl <= l);
      }
    }
  }
}

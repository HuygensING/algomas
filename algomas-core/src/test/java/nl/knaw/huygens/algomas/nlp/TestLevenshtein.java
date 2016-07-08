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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLevenshtein {
  private static class Case {
    final String a, b;
    final int expected;

    Case(String s, String t, int d) {
      a = s;
      b = t;
      expected = d;
    }

    void testDistance() {
      int d = Levenshtein.distance(a, b);
      assertEquals(a + " " + b, expected, d);
      d = Levenshtein.distance(b, a);
      assertEquals(a + " " + b, expected, d);
    }

    void testBounded(int max) {
      int d = Levenshtein.boundedDistance(a, b, max);
      if (expected <= max) {
        assertEquals(a + " " + b, expected, d);
      } else {
        assertEquals(a + " " + b, max, d);
      }
    }
  }

  private final Case[] cases = {
    new Case("bar", "bra", 2),
    new Case("bar", "bard", 1),
    new Case("bar", "br", 1),
    new Case("bar", "car", 1),
    new Case("bar", "foobar", 3),
    new Case("blabla text", "looking for a test", 13),
    new Case("1foo2", "0foo3", 2),
    new Case("1f4oo2", "0foo3", 3),

    // Corner cases.
    new Case("", "", 0),
    new Case("", "foo", 3),
    new Case("super", "super", 0),
    new Case("foobar", "123456", 6),
    new Case("prefixAAsuffix", "prefixBsuffix", 2),
  };

  @Test
  public void runCases() {
    for (Case c : cases) {
      c.testDistance();
    }
  }

  @Test
  public void bounded() {
    for (Case c : cases) {
      c.testBounded(1);
      c.testBounded(2);
      c.testBounded(3);
      c.testBounded(5);
      c.testBounded(100);
      c.testBounded(Integer.MAX_VALUE);
    }
  }
}

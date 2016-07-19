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

public class TestStringSlice {
  @Test
  public void sliceString() {
    String s = "abbcccdddd";
    assertEquals("a", slice(s, 0, 1));
    assertEquals("bb", slice(s, 1, 3));
    assertEquals("ccc", slice(s, 3, 6));
    assertEquals("dddd", slice(s, 6, s.length()));
  }

  private static String slice(String s, int from, int to) {
    return new StringSlice(s, from, to).toString();
  }

  @Test
  public void subSequence() {
    String s = "hello_world";
    CharSequence sub = new StringSlice(s, 0, 6);
    sub = sub.subSequence(5, 6);
    assertEquals("_", sub.toString());
    sub = sub.subSequence(0, 0);
    assertEquals("", sub.toString());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void ofEmpty() {
    new StringSlice("", 1, 1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void ofShortSlice() {
    new StringSlice("foobar", 1, 1).subSequence(2, 3);
  }
}

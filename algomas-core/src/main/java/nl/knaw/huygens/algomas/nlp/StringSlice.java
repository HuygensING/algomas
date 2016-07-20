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

/**
 * Constant-memory and constant-time replacement for String.substring.
 * <p>
 * In Java 7, String.substring changed to a linear-memory implementation (a copy). This class replaces that method
 * for applications where such copying behavior is not acceptable.
 * <p>
 * TODO: make this public?
 */
final class StringSlice implements CharSequence {
  private final String s;
  private final int offset, len;

  StringSlice(String s, int from, int to) {
    this(from, to, s);
    check(from, to, s);
  }

  // No-check constructors. Arguments swapped to change the signature.
  private StringSlice(int from, int to, String s) {
    this.s = s;
    offset = from;
    len = to - from;
  }

  @Override
  public final char charAt(int index) {
    return s.charAt(offset + index);
  }

  private static void check(int from, int to, CharSequence s) {
    if (from > to || from < 0 || to > s.length()) {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public final int length() {
    return len;
  }

  @Override
  public final CharSequence subSequence(int from, int to) {
    check(from, to, this);
    return new StringSlice(offset + from, offset + to, s);
  }

  public final String toString() {
    return s.substring(offset, offset + len);
  }
}

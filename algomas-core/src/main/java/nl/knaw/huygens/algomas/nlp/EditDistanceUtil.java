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

// Common utilities for string edit distances.
class EditDistanceUtil {
  static int commonPrefix(CharSequence a, CharSequence b) {
    int n = Math.min(a.length(), b.length());
    for (int i = 0; i < n; i++) {
      if (a.charAt(i) != b.charAt(i)) {
        return i;
      }
    }
    return n;
  }

  static int commonSuffix(CharSequence a, CharSequence b,
                          int start) {
    int m = a.length() - start, n = b.length() - start;
    int min = Math.min(m, n);
    for (int i = 0; i < min; i++) {
      if (a.charAt(start + m - i - 1) != b.charAt(start + n - i - 1)) {
        return i;
      }
    }
    return min;
  }
}

package nl.knaw.huygens.algomas;

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

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.ToDoubleFunction;

/**
 * Functional programming utilities.
 */
public class Functional {
  /**
   * Turn an Iterator into an Iterable.
   * <p>
   * This supports the idiom
   * <pre>
   *     for (T x : in(iterator)) { process(x); }
   * </pre>
   *
   * @param iter
   * @param <T>
   * @return
   */
  public static <T> Iterable<T> in(Iterator<T> iter) {
    return () -> iter;
  }
}

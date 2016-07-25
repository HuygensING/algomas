package nl.knaw.huygens.algomas.spatial;

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

// Functional interface for metrics. Implementation are expected to abide by the metric axioms.

/**
 * A metric is a function that measures the distance between two objects.
 * <p>
 * Implementations of this functional interface are expected to abide by the
 * metric axioms, i.e., for all {@code x}, {@code y}, {@code z}:
 * <p>
 * {@code distance(x, y) >= 0}
 * <p>
 * {@code distance(x, y) == 0} iff {@code x.equals(y)}
 * <p>
 * {@code distance(x, z) <= distance(x, y) + distance(y, z)}
 */
public interface Metric<T> {
  double distance(T a, T b);
}

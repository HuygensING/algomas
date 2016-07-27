package nl.knaw.huygens.algomas.concurrent;

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

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * A lazily initialized value of type T.
 * <p>
 * A Lazy constructed from a Supplier calls that Supplier's get() method
 * at most once, caching the value, even if multiple threads concurrently
 * request the value.
 * <p>
 * This class is similar to the LazyInitializer in Apache Commons Lang.
 * The differences are the API and the fact that a Lazy is serializable.
 * Depending on whether get() has already been called, either the supplier
 * or the value will be serialized, not both. The {@link TransientLazy}
 * class is an alternative implementation that only serializes the supplier.
 */
public class Lazy<T> implements Serializable, Supplier<T> {
  private static final long serialVersionUID = 1L;

  private volatile T value = null;
  private Supplier<T> supplier;

  public Lazy(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    if (value == null) {
      synchronized (this) {
        if (value == null) {
          value = supplier.get();
          supplier = null; // Don't serialize supplier.
        }
      }
    }
    return value;
  }
}

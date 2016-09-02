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
 * A TransientLazy constructed from a Supplier calls that Supplier's get() method
 * and caches the value. Unlike an ordinary {@link Lazy}, it will not preserve the
 * value across serialization and deserialization, instead recomputing it upon
 * deserialization. This means that the value computed by the supplier need not be
 * serializable.
 * <p>
 * If you are going to serialize a TransientLazy constructed from a lambda, don't
 * forget to make it serializable by casting to an intersection type:
 * <pre>
 * {@code
 * TransientLazy((Supplier<String> & Serializable) () -> readBigFile(path))
 * }
 * </pre>
 */
public class TransientLazy<T> implements Serializable, Supplier<T> {
  private static final long serialVersionUID = 1L;

  private transient volatile T value = null;
  private Supplier<T> supplier;

  public TransientLazy(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    T result = value;
    if (result == null) {
      synchronized (this) {
        result = value;
        if (result == null) {
          value = result = supplier.get();
        }
      }
    }
    return result;
  }
}

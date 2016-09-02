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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
   */
  public static <T> Iterable<T> in(Iterator<T> iter) {
    return () -> iter;
  }

  /**
   * The uncheck methods turn throwing consumers/functions/suppliers into
   * ordinary ones by wrapping them in code that translates checked exceptions
   * into unchecked ones.
   * <p>
   * An {@link IOException} becomes an {@link UncheckedIOException}; any other
   * {@link Exception} becomes a {@link RuntimeException} with the original
   * exception as its cause.
   */
  public static <T> Consumer<T> uncheck(ThrowingConsumer<T> consumer) {
    return (x) -> {
      try {
        consumer.accept(x);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static <T, R> Function<T, R> uncheck(ThrowingFunction<T, R> function) {
    return (x) -> {
      try {
        return function.apply(x);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static <T> Supplier<T> uncheck(ThrowingSupplier<T> supplier) {
    return () -> {
      try {
        return supplier.get();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  @FunctionalInterface
  public interface ThrowingConsumer<T> {
    void accept(T arg) throws Exception;
  }

  @FunctionalInterface
  public interface ThrowingFunction<T, R> {
    R apply(T arg) throws Exception;
  }

  @FunctionalInterface
  public interface ThrowingSupplier<T> {
    T get() throws Exception;
  }
}

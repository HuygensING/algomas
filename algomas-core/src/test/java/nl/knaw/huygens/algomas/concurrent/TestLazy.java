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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.google.common.primitives.Ints.asList;
import static java.util.stream.IntStream.range;
import static nl.knaw.huygens.algomas.Functional.uncheck;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class TestLazy {
  private void singleThread(UnaryOperator<Supplier<CharSequence>> newLazy) {
    Supplier<CharSequence> hello = () -> {
      StringBuilder sb = new StringBuilder();
      sb.append("hello, ");
      sb.append("world!");
      return sb;
    };

    Supplier<CharSequence> lazy = newLazy.apply(hello);
    assertTrue(lazy.get() == lazy.get());
  }

  @Test
  public void singleThreadLazy() {
    singleThread(Lazy::new);
  }

  @Test
  public void singleThreadTransientLazy() {
    singleThread(TransientLazy::new);
  }

  private static final int NTHREADS = 100;

  private void multiThread(UnaryOperator<Supplier<Integer>> newLazy) {
    AtomicInteger nCalls = new AtomicInteger();
    Supplier<Integer> lazy = newLazy.apply(uncheck(() -> {
      Thread.sleep(40);
      int i = nCalls.incrementAndGet();
      Thread.sleep(40);
      return i;
    }));

    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < NTHREADS; i++) {
      threads.add(new Thread(lazy::get));
    }
    threads.forEach(Thread::start);

    try {
      for (Thread thread : threads) {
        thread.join();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    assertTrue(nCalls.get() == 1);
  }

  @Test
  public void multiThreadLazy() {
    multiThread(Lazy::new);
  }

  @Test
  public void multiThreadTransientLazy() {
    multiThread(TransientLazy::new);
  }

  private static <T> T deserialize(byte[] buf) throws Exception {
    return (T) new ObjectInputStream(new ByteArrayInputStream(buf)).readObject();
  }

  private byte[] serialize(Object obj) throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    new ObjectOutputStream(buf).writeObject(obj);
    return buf.toByteArray();
  }

  @Test
  public void serializeTransientLazy() throws Exception {
    TransientLazy<List<Integer>> lazy = new TransientLazy<>((Supplier<List<Integer>> & Serializable) () ->
      asList(range(0, 10000).toArray()));
    final List<Integer> list = lazy.get(); // Force evaluation

    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    new ObjectOutputStream(buf).writeObject(lazy);
    byte[] serialized = serialize(lazy);

    assertTrue(serialized.length < 1000);

    lazy = deserialize(buf.toByteArray());
    List<Integer> newList = lazy.get();
    assertArrayEquals(list.toArray(), newList.toArray());
  }
}

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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.util.Collections.shuffle;

public class TestSort {
  private final int[] seeds = {1, 6, 161, 16, 0, 9612, 126, 778, 125, 991};

  @Test
  public void partialSortIntegers() {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < 1217; i++) {
      list.add(i);
    }

    for (int k = 0; k < 48; k += 3) {
      for (int seed : seeds) {
        shuffle(list, new Random(seed));
        Sort.partial(list, k);
        for (int i = 0; i < k; i++) {
          Assert.assertEquals(i, list.get(i).intValue());
        }
      }
    }
  }

  @Test
  public void selectStrings() {
    List<String> list = Arrays
      .asList("foo", "bar", "baz", "foobar", "quux", "quuux", "ratata", "xyzzy");

    for (int seed : seeds) {
      shuffle(list, new Random(seed));
      Sort.select(list, 0);
      Assert.assertEquals("bar", list.get(0));

      Sort.select(list, 2);
      Assert.assertEquals("foo", list.get(2));

      Sort.select(list, list.size() - 1);
      Assert.assertEquals("xyzzy", list.get(list.size() - 1));
    }
  }
}

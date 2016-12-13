package nl.knaw.huygens.algomas.stat;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SplittableRandom;

import static org.junit.Assert.assertEquals;

public class TestRandomGen {
  @Test
  public void testShuffle() {
    List<String> l = new ArrayList<>();
    l.add("foo");
    l.add("bar");
    l.add("baz");
    Set<String> all = new HashSet<>(l);

    RandomGen.shuffle(l, (i) -> 0);
    assertEquals(all, new HashSet<>(l));

    RandomGen.shuffle(l, new Random(42)::nextInt);
    assertEquals(all, new HashSet<>(l));

    RandomGen.shuffle(l, new SplittableRandom(42)::nextInt);
    assertEquals(all, new HashSet<>(l));
  }
}

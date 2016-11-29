package nl.knaw.huygens.algomas.editdist;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TestLevenshtein {
  @Test
  public void smokeTest() {
    Levenshtein<Integer> lev = new Levenshtein<>(2, 3);

    List<Integer> a = asList(1, 2, 3, 5, 6);
    List<Integer> b = asList(2, 3, 4, 6, 7);

    assertEquals(7, lev.distance(a, b));

    // LCS distance.
    lev = new Levenshtein<>(1, Integer.MAX_VALUE);
    a = asList(1, 2, 4, 5, 6, 7);
    b = asList(2, 3, 4, 5, 6, 8, 9);
    assertEquals(5, lev.distance(a, b));
  }
}

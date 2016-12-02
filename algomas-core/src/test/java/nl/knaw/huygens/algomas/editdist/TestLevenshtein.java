package nl.knaw.huygens.algomas.editdist;

import com.google.common.primitives.Chars;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TestLevenshtein {
  @Test
  public void smokeTest() {
    GenericLevenshtein<Integer> lev = new GenericLevenshtein<>(2, 3);

    List<Integer> a = asList(1, 2, 3, 5, 6);
    List<Integer> b = asList(2, 3, 4, 6, 7);

    assertEquals(7, lev.distance(a, b));
    assertEquals(7, lev.distance(b, a));

    // LCS distance.
    lev = new GenericLevenshtein<>(1, Integer.MAX_VALUE);
    a = asList(1, 2, 4, 5, 6, 7);
    b = asList(2, 3, 4, 5, 6, 8, 9);
    assertEquals(5, lev.distance(b, a));
  }

  @Test
  public void testCustomSubclass() {
    AbstractLevenshtein<Character> custom = new AbstractLevenshtein<Character>() {
      @Override
      protected int indelCost(Character x) {
        switch (x) {
          case 'a':
            return 1;
          case 'b':
            return 2;
          default:
            throw new IllegalArgumentException();
        }
      }

      @Override
      protected int substCost(Character x, Character y) {
        char cx = x, cy = y;
        return cx == cy ? 0 : Integer.MAX_VALUE;
      }
    };

    List<Character> a = aslist("baabbaabbbab");
    List<Character> b = aslist("aaabaaabbbaa");
    assertEquals(9, custom.distance(a, b));
    assertEquals(9, custom.distance(b, a));

    a = aslist("");
    b = aslist("b");
    assertEquals(2, custom.distance(a, b));
    assertEquals(2, custom.distance(b, a));

    a = aslist("a");
    b = aslist("b");
    assertEquals(3, custom.distance(a, b));
    assertEquals(3, custom.distance(b, a));

    a = aslist("a");
    b = aslist("");
    assertEquals(1, custom.distance(a, b));
    assertEquals(1, custom.distance(b, a));
  }

  private static List<Character> aslist(String s) {
    return Chars.asList(s.toCharArray());
  }
}

package nl.knaw.huygens.algomas.spatial;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

// TODO move more of Test{BK,VP}Tree to this class.
public class BaseTestSpatialTree {
  protected static final List<String> QUERY_WORDS = asList("static",
    "final", "ImmutableList<String>", "String", "Levenshtein",
    "Damerau", "Wagner", "Fischer", "Kruskal", "Wallis", "XYZZYFLUX",
    "tree", "distance", "public", "private", "AtomicInteger", "Assert",
    "filter", "map", "expected", "size", "words", "void", "BKTree",
    "DamerauLevenshtein", "assertEquals", "concurrent", "atomic",
    "class", "Java", "Builder", "Guava", "Apache", "Commons-lang",
    "Python", "C", "C++", "Groovy", "Jython", "John Doe", "Jane Doe",
    "Billybob", "ampersand", "edit distance", "VP-tree", "indel cost",
    "transposition", "macromolecule", "time warping", "0123456789");

  protected static final List<String> WORDS = new ArrayList<>();

  static {
    WORDS.addAll(QUERY_WORDS);
    QUERY_WORDS.stream()
               .flatMap(x -> QUERY_WORDS.stream().map(y -> x + " -- " + y))
               .forEachOrdered(WORDS::add);
    WORDS.addAll(asList("foo", "bar", "baz", "quux"));
  }
}

package nl.knaw.huygens.algomas.editdist;

/**
 * Generic edit distance.
 * <p>
 * Unlike the Levenshtein and LevenshteinDamerau classes in package nlp,
 * instances of this class are parameterized on the input type and on
 * atomic edit operation costs.
 */
public class Levenshtein<T> extends AbstractLevenshtein<T> {
  private final int indelCost;
  private final int substCost;

  public Levenshtein() {
    this(1, 1);
  }

  public Levenshtein(int indelCost, int substCost) {
    this.indelCost = indelCost;
    this.substCost = substCost;
  }

  @Override
  protected final int indelCost(T x) {
    return indelCost;
  }

  @Override
  protected final int substCost(T x, T y) {
    return substCost;
  }
}

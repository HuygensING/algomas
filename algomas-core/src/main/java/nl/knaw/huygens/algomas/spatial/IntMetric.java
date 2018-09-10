package nl.knaw.huygens.algomas.spatial;

public interface IntMetric<T> {
  int distance(T a, T b);
}

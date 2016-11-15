package nl.knaw.huygens.algomas.spatial;

import java.io.Serializable;
import java.util.stream.Stream;

public interface SpatialIndex<T> {
  Stream<Entry<T>> nearestNeighbors(int k, T point);

  Stream<Entry<T>> nearestNeighbors(int k, double radius, T point);

  Stream<Entry<T>> withinRadius(T point, double radius);

  Stream<T> stream();

  int size();

  /**
   * Represents one result from a neighborhood query.
   * <p>
   * An {@code Entry} instance packages a point and its distance from a query point.
   * The instance does not record the query point itself.
   */
  final class Entry<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public final double distance;
    public final T point;

    Entry(T point, double distance) {
      this.distance = distance;
      this.point = point;
    }

    public String toString() {
      return String.format("%s at distance %g", point, distance);
    }
  }
}

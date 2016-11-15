package nl.knaw.huygens.algomas.spatial;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Density-based spatial clustering of applications with noise (Dbscan).
 *
 * @param <T> Type of points to be clustered.
 */
public class Dbscan<T> {
  private int nclusters = 0;
  private final Map<T, Integer> clusterIndex = new HashMap<>();
  private final int minPoints;
  private final double radius;

  public Dbscan(Iterable<T> points, SpatialIndex<T> index, double radius, int minPoints) {
    this.minPoints = minPoints;
    this.radius = radius;

    Set<T> visited = new HashSet<>();

    for (T point : points) {
      if (visited.contains(point)) {
        continue;
      }
      visited.add(point);

      Deque<T> neighborhood = query(index, point).collect(Collectors.toCollection(ArrayDeque::new));
      if (neighborhood.size() < minPoints) {
        clusterIndex.put(point, -1);
      } else {
        expand(index, point, neighborhood, clusterIndex, nclusters++, visited);
      }
    }
  }

  private void expand(SpatialIndex<T> index, T point, Deque<T> neighborhood, Map<T, Integer> clusterIndex,
                      int nclusters, Set<T> visited) {
    clusterIndex.put(point, nclusters);

    while (!neighborhood.isEmpty()) {
      T neighbor = neighborhood.pop();
      if (visited.contains(neighbor)) {
        continue;
      }
      visited.add(neighbor);

      List<T> moreNeighbors = query(index, neighbor).collect(Collectors.toList());
      if (moreNeighbors.size() >= minPoints) {
        neighborhood.addAll(moreNeighbors);
      }
      if (!clusterIndex.containsKey(neighbor)) {
        clusterIndex.put(neighbor, nclusters);
      }
    }
  }

  private Stream<T> query(SpatialIndex<T> index, T point) {
    return index.withinRadius(point, radius).map(entry -> entry.point);
  }

  /**
   * Mapping of points to cluster indices.
   *
   * @return A Map that maps each point in the input to an integer label < numClusters().
   *         Noise points have the label -1. The caller is not supposed to modify this map.
   */
  public Map<T, Integer> clusterLabels() {
    return Collections.unmodifiableMap(clusterIndex);
  }

  /**
   * Returns the number of clusters found in the input data, not counting noise.
   */
  public int numClusters() {
    return nclusters;
  }
}

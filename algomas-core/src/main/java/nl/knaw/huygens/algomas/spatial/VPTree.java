package nl.knaw.huygens.algomas.spatial;

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

import nl.knaw.huygens.algomas.stat.RandomGen;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Spliterator;
import java.util.SplittableRandom;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.stream.IntStream.range;

/**
 * A vantage point tree is a spatial index that works with any distance metric.
 * <p>
 * A VP-tree is constructed from a set of points P and can then be queried for the
 * nearest neighbors of a point q in P, or all points in P that are within a fixed
 * distance from q.
 *
 * @param <T> The type of points.
 */
public final class VPTree<T> implements Iterable<T>, Serializable {
  private static final long serialVersionUID = 1L;

  // Construction algorithm.
  @SuppressWarnings("serial")
  private final class ConstructTask extends RecursiveTask<Node<T>> {
    double[] dist;
    List<T> points;
    final SplittableRandom rnd;

    public ConstructTask(SplittableRandom rnd, List<T> points) {
      super();
      this.points = points;
      this.rnd = rnd;
    }

    @Override
    protected final Node<T> compute() {
      int nPoints = points.size();
      if (nPoints == 0) {
        return null;
      }

      dist = new double[nPoints - 1];

      // Coarsened base cases.
      switch (nPoints) {
        case 1:
          return singleton(points.get(0));
        case 2:
          T vantage;
          T other;
          if (rnd.nextBoolean()) {
            vantage = points.get(0);
            other = points.get(1);
          } else {
            vantage = points.get(1);
            other = points.get(0);
          }
          return new Node<>(vantage, metric.distance(vantage, other),
            singleton(other), null);
        case 3:
          return construct3();
        default:
      }

      T vantagePoint = selectVantage();

      range(0, points.size())
        .parallel()
        .forEach(i -> dist[i] = metric.distance(points.get(i), vantagePoint));

      int medianIndex = selectMedian();
      double medianDistance = dist[medianIndex];
      dist = null;

      ConstructTask left = new ConstructTask(rnd.split(),
        points.subList(0, medianIndex));
      left.fork();
      Node<T> outside = new ConstructTask(rnd,
        points.subList(medianIndex, points.size())).compute();
      Node<T> inside = left.tryUnfork() ? left.compute() : left.join();

      return new Node<>(vantagePoint, medianDistance, inside, outside);
    }

    // Brute-force specialization of construct() for points.size() == 3.
    private Node<T> construct3() {
      T p0 = points.get(0);
      T p1 = points.get(1);
      T p2 = points.get(2);
      double d01 = metric.distance(p0, p1);
      double d02 = metric.distance(p0, p2);
      double d12 = metric.distance(p1, p2);

      int bestIndex = 0;
      double mean = .5 * (d01 + d02);
      double bestSpread = abs(d01 - mean) + abs(d02 - mean);

      mean = (d01 + d12) / 2;
      double spread = abs(d01 - mean) + abs(d12 - mean);
      if (spread > bestSpread) {
        bestIndex = 1;
        bestSpread = spread;
      }

      mean = (d02 + d12) / 2;
      spread = abs(d02 - mean) + abs(d12 - mean);
      if (spread > bestSpread) {
        bestIndex = 2;
      }

      switch (bestIndex) {
        case 0:
          dist[0] = d01;
          dist[1] = d02;
          break;
        case 1:
          dist[0] = d01;
          dist[1] = d12;
          break;
        case 2:
          dist[0] = d02;
          dist[1] = d12;
          break;
        default:
      }

      T vantagePoint = points.get(bestIndex);
      points.set(bestIndex, points.get(2));
      if (dist[0] > dist[1]) {
        swap(0, 1);
      }

      return new Node<>(vantagePoint, dist[0], singleton(points.get(0)),
        singleton(points.get(1)));
    }

    // Quickselect. Points has been shuffled (by selectVantage) before
    // entry, so no need to bother with fancy pivoting.
    private int selectMedian() {
      int medianIndex = dist.length / 2;
      for (int lo = 0, hi = points.size() - 1; hi > lo; ) {
        int pivotIndex = partition(lo, hi);
        if (pivotIndex == medianIndex) {
          break;
        } else if (medianIndex < pivotIndex) {
          hi = pivotIndex - 1;
        } else {
          lo = pivotIndex + 1;
        }
      }
      return medianIndex;
    }

    // Lomuto partition. Partitions dist[lo:hi] and points[lo:hi] around a
    // pivot value.
    private int partition(int lo, int hi) {
      double pivot = dist[hi];

      int i = lo;
      for (int j = lo; j < hi; j++) {
        if (dist[j] <= pivot) {
          swap(i, j);
          i++;
        }
      }

      swap(i, hi);
      return i;
    }

    private void swap(int i, int j) {
      double temp = dist[i];
      dist[i] = dist[j];
      dist[j] = temp;
      points.set(i, points.set(j, points.get(i)));
    }

    // Selects and removes a vantage point from points.
    private T selectVantage() {
      // The first sampleSize points are the candidates. Taking ~sqrt(N)
      // as the sample size ensures that we make a linear number of
      // distance comparisons.
      int sampleSize = (int) sqrt(points.size());
      RandomGen.shuffle(points, rnd::nextInt);
      List<T> rest = points.subList(sampleSize, points.size());

      int bestIndex = -1;
      double bestSpread = Double.NEGATIVE_INFINITY;

      sampleSize = (int) sqrt(rest.size());

      for (int i = 0; i < sampleSize; i++) {
        T candidate = points.get(i);
        int start = i * sampleSize;
        int end = (i + 1) * sampleSize;

        range(start, end)
          .parallel()
          .forEach(j -> dist[j] = metric.distance(rest.get(j), candidate));

        double mean = Arrays.stream(dist, start, end).average().getAsDouble();
        // spread = mean absolute deviation.
        double spread = Arrays.stream(dist, start, end)
                              .map(d -> abs(d - mean)).average().getAsDouble();

        if (spread > bestSpread) {
          bestIndex = i;
          bestSpread = spread;
        }
      }

      T vantagePoint = points.get(bestIndex);
      points.set(bestIndex, points.get(points.size() - 1));
      points = points.subList(0, points.size() - 1);
      return vantagePoint;
    }

    private Node<T> singleton(T point) {
      return new Node<>(point, Double.NaN, null, null);
    }
  }

  /**
   * Represents one result from a neighborhood query.
   * <p>
   * An {@code Entry} instance packages a point and its distance from a query point.
   * The instance does not record the query point itself.
   */
  public static final class Entry<T> implements Serializable {
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

  private static final class Node<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    final T center;
    final double radius;
    final Node<T> inside;
    final Node<T> outside;

    Node(T point, double radius, Node<T> inside, Node<T> outside) {
      center = point;
      this.radius = radius;
      this.inside = inside;
      this.outside = outside;
    }
  }

  private final Metric<T> metric;
  private final Node<T> root;

  public VPTree(Metric<T> metric, Iterable<T> points) {
    this(metric, points, new SplittableRandom());
  }

  /**
   * Construct VPTree from given points.
   *
   * @param metric Metric (distance function).
   * @param points Collection of points to store in the tree.
   * @param rnd    Random number generator.
   */
  public VPTree(Metric<T> metric, Iterable<T> points, SplittableRandom rnd) {
    this(metric, StreamSupport.stream(points.spliterator(), false), rnd);
  }

  public VPTree(Metric<T> metric, Stream<T> points, SplittableRandom rnd) {
    this.metric = metric;
    root = ForkJoinPool.commonPool()
                       .invoke(new ConstructTask(rnd, points.collect(Collectors.toList())));
  }

  public Metric<T> getMetric() {
    return metric;
  }

  public boolean isEmpty() {
    return root == null;
  }

  @Override
  public Iterator<T> iterator() {
    return stream().iterator();
  }

  private static class SpliteratorImpl<T> implements Spliterator<T> {
    // Queue of nodes still to be traversed. Since we push higher nodes
    // before lower ones, FIFO order should correspond roughly to ordering
    // by size.
    private ArrayDeque<Node<T>> nodes = new ArrayDeque<>();
    private int size;

    private SpliteratorImpl(Node<T> node, int size) {
      nodes.addLast(node);
      this.size = (node.inside == null && node.outside == null) ? 1 : size;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      if (nodes.isEmpty()) {
        return false;
      }
      Node<T> top = nodes.removeFirst();
      action.accept(top.center);
      push(top.inside);
      push(top.outside);
      size--;
      return true;
    }

    private void push(Node<T> node) {
      if (node != null) {
        nodes.addLast(node);
      }
    }

    @Override
    public Spliterator<T> trySplit() {
      if (nodes.isEmpty()) {
        return null;
      }
      // Assume that about half the elements are in the first node on the queue.
      Spliterator<T> left = new SpliteratorImpl<>(nodes.removeFirst(), size / 2);
      size -= size / 2;
      return left;
    }

    @Override
    public long estimateSize() {
      return size;
    }

    @Override
    public int characteristics() {
      return SIZED | SUBSIZED;
    }
  }

  @Override
  public Spliterator<T> spliterator() {
    return new SpliteratorImpl<>(root, size());
  }

  /**
   * Finds the k nearest neighbors of the given point.
   * <p>
   * Returns a stream of entries containing neighbor points and their distance
   * from the query point.
   *
   * @param k     Number of neighbors to collect.
   * @param point Query point.
   */
  public final Stream<Entry<T>> nearestNeighbors(int k, T point) {
    return nearestNeighbors(k, Double.POSITIVE_INFINITY, point);
  }

  /**
   * Finds the k nearest neighbors of the given point, restricted to a search
   * radius.
   * <p>
   * Returns a stream of entries containing neighbor points and their distance
   * from the query point.
   *
   * @param k      Number of neighbors to collect.
   * @param radius Neighbors must be at distance <= radius from the query point.
   * @param point  Query point.
   */
  public final Stream<Entry<T>> nearestNeighbors(int k, double radius, T point) {
    // Max priority queue sorted on distance from query point.
    Comparator<Entry<T>> byDistance = comparing(e -> e.distance);
    PriorityQueue<Entry<T>> nearest = new PriorityQueue<>(k + 1, reverseOrder(byDistance));
    nearestNeighbors(root, k, point, radius, nearest);
    return nearest.stream();
  }

  private double nearestNeighbors(Node<T> n, int k, T point, double radius,
                                  PriorityQueue<Entry<T>> nearest) {
    if (n == null) {
      return radius;
    }

    double d = metric.distance(point, n.center);
    if (d < radius) {
      nearest.offer(new Entry<>(n.center, d));
      if (nearest.size() > k) {
        nearest.poll();
      }
      if (nearest.size() == k) {
        radius = nearest.peek().distance;
      }
    }

    if (d < n.radius) {
      radius = nearestNeighbors(n.inside, k, point, radius, nearest);
      if (d + radius >= n.radius) {
        radius = nearestNeighbors(n.outside, k, point, radius, nearest);
      }
    } else {
      radius = nearestNeighbors(n.outside, k, point, radius, nearest);
      if (d - radius <= n.radius) {
        radius = nearestNeighbors(n.inside, k, point, radius, nearest);
      }
    }
    return radius;
  }

  /**
   * Finds all points within the given radius of the given point.
   * <p>
   * Returns a stream of entries containing neighbor points and their distance
   * from the query point.
   *
   * @param radius Neighbors must be at distance <= radius from the query point.
   * @param point  Query point.
   */
  public Stream<Entry<T>> withinRadius(T point, double radius) {
    List<Entry<T>> result = new ArrayList<>();
    withinRadius(root, point, radius, result);
    return result.stream();
  }

  private void withinRadius(Node<T> n, T point, double radius, List<Entry<T>> result) {
    if (n == null) {
      return;
    }

    double d = metric.distance(point, n.center);
    if (d < radius) {
      result.add(new Entry<>(n.center, d));
    }

    if (d < n.radius) {
      withinRadius(n.inside, point, radius, result);
      if (d + radius >= n.radius) {
        withinRadius(n.outside, point, radius, result);
      }
    } else {
      withinRadius(n.outside, point, radius, result);
      if (d - radius <= n.radius) {
        withinRadius(n.inside, point, radius, result);
      }
    }
  }

  public int size() {
    return size(root);
  }

  private static <T> int size(Node<T> n) {
    if (n == null) {
      return 0;
    }
    return size(n.inside) + 1 + size(n.outside);
  }

  /**
   * A stream containing all points within the tree.
   */
  public Stream<T> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}

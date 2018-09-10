package nl.knaw.huygens.algomas.spatial;

/*
 * #%L
 * algomas-core
 * %%
 * Copyright (C) 2018 KNAW
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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

/**
 * A BK-tree is a spatial index that speeds up neighbor queries in discrete metric spaces,
 * such as the space of strings with Levenshtein distance.
 *
 * BK-trees are less efficient than VP-trees ({@link VPTree}), but they allow insertion of
 * new elements after construction.
 *
 * @param <T> The type of points.
 */
public class BKTree<T> implements Collection<T> {
  private static class Node<T> {
    final T key;
    TreeMap<Integer, Node<T>> sub;

    Node(T key) {
      this.key = key;
    }
  }

  private final boolean allowDuplicates;
  private int nelems = 0;
  private Node<T> root = null;

  private IntMetric<T> metric;

  public BKTree(IntMetric<T> metric) {
    this(metric, true);
  }

  public BKTree(IntMetric<T> metric, boolean allowDuplicates) {
    this(metric, allowDuplicates, Collections.emptyList());
  }

  public BKTree(IntMetric<T> metric, Iterable<T> points) {
    this(metric, true, points);
  }

  public BKTree(IntMetric<T> metric, boolean allowDuplicates, Iterable<T> points) {
    this.allowDuplicates = allowDuplicates;
    this.metric = metric;
    points.forEach(this::add);
  }

  @Override
  public boolean add(T key) {
    if (key == null) {
      throw new NullPointerException("null key not allowed");
    }

    if (root == null) {
      root = new Node<T>(key);
      nelems++;
      return true;
    }

    for (Node<T> node = root; ; ) {
      int d = metric.distance(key, node.key);
      if (d == 0 && !allowDuplicates) {
        return false;
      }
      if (node.sub == null) {
        node.sub = new TreeMap<>();
      }
      Node<T> subd = node.sub.get(d);
      if (subd == null) {
        node.sub.put(d, new Node<>(key));
        break;
      }
      node = subd;
    }
    nelems++;
    return true;
  }


  @Override
  public boolean addAll(Collection<? extends T> c) {
    return c.stream().map(this::add).reduce(Boolean::logicalOr).orElse(false);
  }

  @Override
  public void clear() {
    nelems = 0;
    root = null;
  }

  @Override
  public boolean contains(Object o) {
    //noinspection unchecked
    return withinRadius((T) o, 0).findAny().isPresent();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return c.stream().map(this::contains).reduce(Boolean::logicalAnd).orElse(true);
  }

  @Override
  public boolean isEmpty() {
    return root == null;
  }

  @Override
  public Iterator<T> iterator() {
    return stream().iterator();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return nelems;
  }

  private static class SpliteratorImpl<T> extends Spliterators.AbstractSpliterator<T> {
    private ArrayDeque<Node<T>> nodes = new ArrayDeque<>();
    private int nChildren, size;

    public SpliteratorImpl(Node<T> node, int size) {
      super(node.sub.isEmpty() ? 1 : size, SIZED | SUBSIZED);
      nodes.add(node);
      nChildren = node.sub.size();
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      if (nodes.isEmpty()) {
        return false;
      }
      Node<T> top = nodes.removeFirst();
      action.accept(top.key);
      if (top.sub != null) {
        nodes.addAll(top.sub.values());
      }
      size--;
      return true;
    }

    @Override
    public Spliterator<T> trySplit() {
      if (nodes.isEmpty() || nChildren == 0) {
        return null;
      }
      // Assume tree is completely balanced.
      Node<T> first = nodes.removeFirst();
      int leftSize = size / nChildren;
      Spliterator<T> left = new SpliteratorImpl<>(first, leftSize);
      size -= leftSize;
      nChildren--;
      return left;
    }
  }

  @Override
  public Spliterator<T> spliterator() {
    return new SpliteratorImpl<>(root, nelems);
  }

  @Override
  public Object[] toArray() {
    return new Object[0];
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return null;
  }

  public static class Entry<T> {
    public final int distance;
    public final T point;

    Entry(T point, int distance) {
      this.distance = distance;
      this.point = point;
    }
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
    return nearestNeighbors(k, Integer.MAX_VALUE, point);
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
  public final Stream<Entry<T>> nearestNeighbors(int k, int radius, T point) {
    if (root == null) {
      return Stream.empty();
    }
    Comparator<Entry<T>> byDistance = comparing(e -> e.distance);
    PriorityQueue<Entry<T>> nearest = new PriorityQueue<>(k + 1, reverseOrder(byDistance));
    nearestNeighbors(root, k, radius, point, nearest);
    return nearest.stream();
  }

  private int nearestNeighbors(Node<T> node, int k, int radius, T point, PriorityQueue<Entry<T>> nearest) {
    int d = metric.distance(point, node.key);
    if (d <= radius) {
      nearest.offer(new Entry<>(node.key, d));
      if (nearest.size() > k) {
        nearest.poll();
      }
      if (nearest.size() == k) {
        radius = nearest.peek().distance;
      }
    }

    if (node.sub != null) {
      int lower = Math.max(d - radius, 0);
      SortedMap<Integer, Node<T>> sub = node.sub.tailMap(lower);

      for (Map.Entry<Integer, Node<T>> child : sub.entrySet()) {
        int upper = (int) Math.min((long) d + radius, Integer.MAX_VALUE);
        if (child.getKey() > upper) {
          break;
        }

        radius = nearestNeighbors(child.getValue(), k, radius, point, nearest);
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
  public final Stream<Entry<T>> withinRadius(T point, int radius) {
    return withinRadius(point, radius, root);
  }

  private Stream<Entry<T>> withinRadius(T point, int radius, Node<T> node) {
    if (node == null) {
      return Stream.empty();
    }
    int d = metric.distance(point, node.key);
    Stream<Entry<T>> result;
    if (d <= radius) {
      result = Stream.of(new Entry<>(node.key, d));
    } else {
      result = Stream.empty();
    }

    if (node.sub != null) {
      int lower = Math.max(d - radius, 0);
      int upper = (int) Math.min((long) d + radius + 1, Integer.MAX_VALUE);
      result = Stream.concat(result,
        node.sub.subMap(lower, upper)
                .values().stream()
                .flatMap(sub -> withinRadius(point, radius, sub)));
    }
    return result;
  }

  // For debugging.
  void display() {
    display(root, -1, 0);
  }

  private static <T> void display(Node<T> node, int dist, int indent) {
    for (int i = 0; i < indent; i++) {
      System.out.write(' ');
    }
    if (dist >= 0) {
      System.out.printf("%d ", dist);
    }
    System.out.println(node.key);
    if (node.sub != null) {
      node.sub.forEach((d, n) -> display(n, d, indent + 4));
    }
  }
}

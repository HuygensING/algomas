package nl.knaw.huygens.algomas.spatial;

import com.google.common.collect.ImmutableSet;
import nl.knaw.huygens.algomas.nlp.Levenshtein;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDBSCAN {
  private static final Set<String> list0 = ImmutableSet.of(
    "keizerlijk", "keyserlijck", "keyserlyck", "kaiserlich"
  );
  private static final Set<String> list1 = ImmutableSet.of(
    "koninklijk", "koninclijck", "koninclyk"
  );
  private static final Set<String> noise = ImmutableSet.of(
    "prinselijk", "princelyck", "hofhouding", "majesteit"
  );

  private static final List<String> allPoints = new ArrayList<>();

  static {
    allPoints.addAll(list0);
    allPoints.addAll(list1);
    allPoints.addAll(noise);
  }

  @Test
  public void testDBSCAN() {
    DBSCAN<String> clustering = new DBSCAN<>(allPoints, new VPTree<>(Levenshtein::distance, allPoints), 5, 3);

    assertEquals(2, clustering.numClusters());

    Collection<String> cluster0 = new HashSet<>();
    Collection<String> cluster1 = new HashSet<>();
    Collection<String> noise = new HashSet<>();

    clustering.clusterLabels().forEach((point, index) -> {
      assertTrue(index >= -1 && index <= 1);
      switch (index) {
        case 0:
          cluster0.add(point);
          break;
        case 1:
          cluster1.add(point);
          break;
        case -1:
          noise.add(point);
          break;
      }
    });
    assertTrue(cluster0.equals(list0) && cluster1.equals(list1)
      || cluster0.equals(list1) && cluster1.equals(list0));
  }
}

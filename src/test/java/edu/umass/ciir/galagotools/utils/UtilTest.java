package edu.umass.ciir.galagotools.utils;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UtilTest {

  @Test
  public void testPopLast() throws Exception {
    ArrayList<Integer> data = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5));

    assertEquals(5, (int) Util.popLast(data));
    assertEquals(4, (int) Util.popLast(data));
    assertEquals(3, (int) Util.popLast(data));
    assertEquals(2, (int) Util.popLast(data));
    assertEquals(1, (int) Util.popLast(data));
    assertNull(Util.popLast(data));
    assertNull(Util.popLast(data));
  }

  @Test
  public void testIntersection() {
    Set<Integer> lhs = new HashSet<Integer>(Arrays.asList(1, 2, 3));
    Set<Integer> rhs = new HashSet<Integer>(Arrays.asList(3, 4, 5));
    Set<Integer> isect = Util.intersection(lhs, rhs);

    assertEquals(1, isect.size());
    assertEquals(3, (int) Util.first(isect));
  }

  @Test
  public void testBatched() {
    List<Integer> raw = Arrays.asList(1,2, 3,4, 5,6, 7);
    List<List<Integer>> batches = Util.batched(raw, 2);
    assertEquals(4,batches.size());
    assertEquals(2,batches.get(0).size());
    assertEquals(2,batches.get(1).size());
    assertEquals(2,batches.get(2).size());
    assertEquals(1,batches.get(3).size());
  }
}
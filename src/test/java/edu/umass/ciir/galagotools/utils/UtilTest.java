package edu.umass.ciir.galagotools.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

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
}
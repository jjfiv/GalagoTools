package edu.umass.ciir.galagotools.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandUtilTest {

  @Test
  public void testFisherYatesStreamSample() throws Exception {
    List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    Random rand = new Random(13);
    List<Integer> samp = RandUtil.sampleRandomly(nums, 3, rand);
    System.out.println(samp);
  }

}
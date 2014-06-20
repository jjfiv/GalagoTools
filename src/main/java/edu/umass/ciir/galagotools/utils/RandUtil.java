package edu.umass.ciir.galagotools.utils;

import org.lemurproject.galago.core.util.FixedSizeMinHeap;
import org.lemurproject.galago.tupleflow.Utility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @author jfoley.
 */
public class RandUtil {

  public static class RandomlyWeighted<T>  {
    public final T obj;
    public final int weight;
    public RandomlyWeighted(T obj, Integer weight) {
      this.obj = obj;
      this.weight = weight;
    }
    public static final Comparator<RandomlyWeighted> byWeight = new Comparator<RandomlyWeighted>() {
      @Override
      public int compare(RandomlyWeighted lhs, RandomlyWeighted rhs) {
        return Utility.compare(lhs.weight, rhs.weight);
      }
    };
  }

  /** fill a heap with randomly weighted elements as you go... */
  public static <T> List<T> sampleRandomly(Iterable<T> source, int count, Random rand) {
    FixedSizeMinHeap<RandomlyWeighted> heap = new FixedSizeMinHeap<RandomlyWeighted>(RandomlyWeighted.class, count, RandomlyWeighted.byWeight);

    for(T newObj : source) {
      int weight = rand.nextInt();
      heap.offer(new RandomlyWeighted<T>(newObj, weight));
    }

    ArrayList<T> output = new ArrayList<T>(count);
    for (RandomlyWeighted rw : heap.getSortedArray()) {
      output.add(Util.<T>cast(rw.obj));
    }

    return output;
  }
}

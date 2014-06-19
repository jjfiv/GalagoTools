package edu.umass.ciir.galagotools.utils;

import org.lemurproject.galago.tupleflow.FileUtility;

import java.io.File;
import java.util.*;

/**
 * @author jfoley
 */
public class Util {
  public static List<File> getChildrenRecursively(File path) {
    List<File> results = new ArrayList<File>();
    if(path.isDirectory()) {
      for(File fp : FileUtility.safeListFiles(path)) {
        results.addAll(getChildrenRecursively(fp));
      }
    } else {
      results.add(path);
    }
    return results;
  }

  public static List<File> checkAndExpandPaths(List<String> paths) {
    List<File> results = new ArrayList<File>(paths.size());
    for(String path : paths) {
      File fp = new File(path);
      if(!fp.exists())
        throw new IllegalArgumentException("File '"+path+"' does not exist!");

      results.addAll(getChildrenRecursively(fp));
    }
    return results;
  }

  public static List<File> collectLines(List<File> files) {
    final ArrayList<File> paths = new ArrayList<File>();
    IO.StringFunctor pathCollector = new IO.StringFunctor() {
      @Override
      public void process(String input) {
        if(!input.trim().isEmpty()) {
          paths.add(new File(input));
        }
      }
    };
    for(File fp : files) {
      IO.forEachLine(fp, pathCollector);
    }
    return paths;
  }

  public static <T> T last(List<T> input) {
    if(input.isEmpty()) return null;
    int lastItem = input.size() - 1;
    return input.get(lastItem);
  }

  public static <T> List<T> take(List<T> input, int count) {
    List<T> output = new ArrayList<T>(count);
    for(int i=0; i<count && i<input.size(); i++) {
      output.add(input.get(i));
    }
    return output;
  }

  /**
   * Efficiently get and remove one from an ArrayList
   * @param input the arraylist
   * @param <T> type parameter
   * @return the last item or null if none
   */
  public static <T> T popLast(List<T> input) {
    if(input.isEmpty()) return null;
    int lastItem = input.size() - 1;
    T last = input.get(lastItem);
    input.remove(lastItem);
    return last;
  }

  public static <T> T first(Set<T> input) {
    if(input.isEmpty()) return null;
    return input.iterator().next();
  }

  public static <T> T first(List<T> input) {
    if(input.isEmpty()) return null;
    return input.get(0);
  }

  public static <T> List<T> rest(List<T> input) {
    if(input.size() < 2) {
      return Collections.emptyList();
    }
    return new ArrayList<T>(input.subList(1, input.size()));
  }

  public static <T> Set<T> intersection(List<Set<T>> sets) {
    if(sets.isEmpty()) return Collections.emptySet();

    Set<T> accum = sets.get(0);
    for(int i=1; i<sets.size(); i++) {
      accum = intersection(accum, sets.get(i));
    }
    return accum;
  }

  public static <T> Set<T> intersection(Set<T> lhs, Set<T> rhs) {
    Set<T> minSet = lhs.size() < rhs.size() ? lhs : rhs;
    Set<T> maxSet = lhs.size() < rhs.size() ? rhs : lhs;

    HashSet<T> isect = new HashSet<T>();
    for(T x : minSet) {
      if(maxSet.contains(x)) {
        isect.add(x);
      }
    }
    return isect;
  }

  public static interface Transform<A,B> {
    public B process(A input) throws Exception;
  }

  public static <T,U> List<U> map(List<T> input, Transform<T,U> transform) {
    ArrayList<U> output = new ArrayList<U>(input.size());
    for(T x : input) {
      try {
        output.add(transform.process(x));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return output;
  }

  public static <T> T[] reverseArray(T[] input) {
    T[] output = Arrays.copyOf(input, input.length);
    for(int i=0; i<input.length; i++) {
      output[input.length-i-1] = input[i];
    }
    return output;
  }
}

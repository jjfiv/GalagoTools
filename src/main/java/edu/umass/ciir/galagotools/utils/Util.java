package edu.umass.ciir.galagotools.utils;

import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.utility.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author jfoley
 */
public class Util {
  public static List<File> getChildrenRecursively(File path) {
    List<File> results = new ArrayList<>();
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
    List<File> results = new ArrayList<>(paths.size());
    for(String path : paths) {
      File fp = new File(path);
      if(!fp.exists())
        throw new IllegalArgumentException("File '"+path+"' does not exist!");

      results.addAll(getChildrenRecursively(fp));
    }
    return results;
  }

  public static List<File> collectLines(List<File> files) {
    final ArrayList<File> paths = new ArrayList<>();
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
    List<T> output = new ArrayList<>(count);
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

  public static <K,V> V firstValue(Map<K,V> input) {
    if(input.isEmpty()) return null;
    return input.values().iterator().next();
  }

  public static <K,V> K firstKey(Map<K,V> input) {
    if(input.isEmpty()) return null;
    return input.keySet().iterator().next();
  }

  public static <T> T first(List<T> input) {
    if(input.isEmpty()) return null;
    return input.get(0);
  }

  public static <T> List<T> rest(List<T> input) {
    if(input.size() < 2) {
      return Collections.emptyList();
    }
    return new ArrayList<>(input.subList(1, input.size()));
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

    HashSet<T> isect = new HashSet<>();
    for(T x : minSet) {
      if(maxSet.contains(x)) {
        isect.add(x);
      }
    }
    return isect;
  }

  @SuppressWarnings("unchecked")
  public static <A,B> Map<A,B> castMap(Map input) {
    return (Map<A,B>) input;
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object obj) {
    return (T) obj;
  }

  @SuppressWarnings("unchecked")
  public static <T> void extendList(Parameters p, String key, Class<T> klazz, T value) {
    if(!p.isList(key)) {
      List<T> lst = new ArrayList<>();
      boolean hasOriginal = p.containsKey(key);
      if(hasOriginal) {
        lst.add((T) p.get(key));
      }

      p.put(key, lst);
    }
    p.getList(key, klazz).add(value);
  }

  public static <K,T> void extendListInMap(Map<K,List<T>> inMap, K key, T value) {
    List<T> existing = inMap.get(key);
    if(existing == null) {
      existing = new ArrayList<>();
      inMap.put(key, existing);
    }
    existing.add(value);
  }

  public static <K,T> void extendSetInMap(Map<K,Set<T>> inMap, K key, T value) {
    Set<T> existing = inMap.get(key);
    if(existing == null) {
      existing = new HashSet<>();
      inMap.put(key, existing);
    }
    existing.add(value);
  }

  public static <T extends Comparable> List<T> sorted(Collection<T> input) {
    List<T> sortable = new ArrayList<>(input);
    Collections.sort(sortable);
    return sortable;
  }

  public static interface Transform<A,B> {
    public B process(A input) throws Exception;
  }

  public static <T,U> List<U> map(List<T> input, Transform<T,U> transform) {
    ArrayList<U> output = new ArrayList<>(input.size());
    for(T x : input) {
      try {
        output.add(transform.process(x));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return output;
  }

  /** process a list in batches of a given size */
  public static <T> List<List<T>> batched(List<T> input, int batchSize) {
    int numElements = input.size();
    int numBatches = numElements/batchSize;
    if(numBatches * batchSize < numElements) {
      numBatches++;
    }
    List<List<T>> batched = new ArrayList<>();

    for(int i=0; i<numBatches; i++) {
      List<T> currentBatch = new ArrayList<>();
      for(int j=0; j<batchSize; j++) {
        int raw = i*batchSize + j;
        if(raw >= numElements) break;
        currentBatch.add(input.get(raw));
      }
      batched.add(currentBatch);
    }

    return batched;
  }

  public static class OneShotIterable<T> implements Iterable<T> {
    private Iterator<T> iter;
    public OneShotIterable(Iterator<T> iter) {
      this.iter = iter;
    }

    @Override
    public Iterator<T> iterator() {
      if(iter == null) throw new IllegalStateException("Can't use a OneShotIterable twice!");
      Iterator<T> prev = iter;
      iter = null;
      return prev;
    }
  }

  public static interface UntilNullGenerator<T> {
    public T next() throws IOException;
  }

  public static abstract class ReadOnlyIterator<T> implements Iterator<T> {
    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public static class UntilNullIterator<T> extends ReadOnlyIterator<T> {
    public final UntilNullGenerator<T> generator;
    public T nextValue;

    public UntilNullIterator(UntilNullGenerator<T> generator) throws IOException {
      this.generator = generator;
      this.nextValue = generator.next();
    }

    @Override
    public boolean hasNext() {
      return nextValue != null;
    }

    @Override
    public T next() {
      try {
        T prevValue = nextValue;
        nextValue = generator.next();
        return prevValue;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

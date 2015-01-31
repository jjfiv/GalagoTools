package edu.umass.ciir.galagotools.tuple;

import java.util.AbstractList;
import java.util.Map;

/**
* @author jfoley
*/
public class Pair<A,B> implements Map.Entry<A,B> {
  public final A left;
  public final B right;

  public Pair(A left, B right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public int hashCode() {
    return left.hashCode() ^ right.hashCode();
  }

  @Override
  public A getKey() {
    return left;
  }

  @Override
  public B getValue() {
    return right;
  }

  @Override
  public B setValue(B b) {
    throw new UnsupportedOperationException("Pair is immutable.");
  }

  @Override
  public String toString() {
    return "["+left+" "+right+"]";
  }

  @Override
  public boolean equals(Object other) {
    if(!(other instanceof Pair)) return false;
    if(other == null) return false;
    Pair cmp = (Pair) other;
    return left.equals(cmp.left) && right.equals(cmp.right);
  }

  public static <X,Y> Pair<X,Y> of(X lhs, Y rhs) {
    return new Pair<>(lhs, rhs);
  }
}

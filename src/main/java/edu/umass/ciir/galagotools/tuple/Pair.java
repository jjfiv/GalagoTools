package edu.umass.ciir.galagotools.tuple;

/**
* @author jfoley
*/
public class Pair<A,B> {
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

package edu.umass.ciir.galagotools.galago;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.tupleflow.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley.
 */
public class WeightedTerm implements Comparable<WeightedTerm> {
  public String term;
  public double weight;
  public WeightedTerm(String term, double weight) {
    this.term = term;
    this.weight = weight;
  }

  @Override
  public String toString() {
    return term +"\t"+weight;
  }

  @Override
  public int compareTo(WeightedTerm other) {
    int byWeight = Utility.compare((int) (weight*1000000.0),(int) (other.weight*1000000.0));
    if(byWeight == 0) return Utility.compare(term, other.term);
    return byWeight;
  }

  public static TObjectDoubleHashMap<String> makeMap() {
    return new TObjectDoubleHashMap<String>();
  }

  public static Node toCombine(List<WeightedTerm> terms) {
    return toCombine(terms, "combine");
  }
  public static Node toCombine(List<WeightedTerm> terms, String op) {
    Node combine = new Node(op);
    for (int i = 0; i < terms.size(); i++) {
      WeightedTerm wt = terms.get(i);
      combine.addChild(Node.Text(wt.term));
      combine.getNodeParameters().set(Integer.toString(i), wt.weight);
    }
    return combine;
  }


  public static final class Comparator implements java.util.Comparator<WeightedTerm> {
    @Override
    public int compare(WeightedTerm lhs, WeightedTerm rhs) {
      return lhs.compareTo(rhs);
    }
  }

  public static List<String> words(List<WeightedTerm> terms) {
    List<String> output = new ArrayList<String>();
    for (WeightedTerm term : terms) {
      output.add(term.term);
    }
    return output;
  }
}

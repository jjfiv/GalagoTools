package edu.umass.ciir.galagotools.scoring;

import org.lemurproject.galago.core.retrieval.RequiredParameters;
import org.lemurproject.galago.core.retrieval.RequiredStatistics;
import org.lemurproject.galago.core.retrieval.iterator.*;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.retrieval.query.AnnotatedNode;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.util.ExtentArray;

import java.io.IOException;

/**
 * @author jfoley.
 */
@RequiredStatistics(statistics = {"collectionLength"})
@RequiredParameters(parameters = {"mu"})
public class FlatSDM extends DisjunctionIterator implements ScoreIterator {
  private final double mu;
  private final LengthsIterator lengthsIter;
  private final ExtentIterator[] unigramIters;

  double term_bg[], od_bg[], uw_bg[];
  private ExtentArray[] pos;
  private double unigramWeight;
  private double bigramWeight;
  private double ubigramWeight;

  public FlatSDM(NodeParameters parameters, LengthsIterator lengthsIter, ExtentIterator[] unigramIters) throws IOException {
    super(unigramIters);
    this.lengthsIter = lengthsIter;
    this.unigramIters = unigramIters;
    assert(unigramIters.length > 1);

    double collectionLength = parameters.getLong("collectionLength");
    this.mu = parameters.get("mu", 2500.0);

    unigramWeight = 0.8;
    bigramWeight = 0.15;
    ubigramWeight = 0.05;

    long startStats = System.currentTimeMillis();
    calculateStats(collectionLength);
    long endStats = System.currentTimeMillis();

    System.out.println("Stats: "+(endStats - startStats));

    this.pos = new ExtentArray[unigramIters.length];
  }

  private void calculateStats(double collectionLength) throws IOException {
    ScoringContext ctxt = new ScoringContext();

    long term_cf[], od_cf[], uw_cf[];

    term_cf = new long[unigramIters.length];
    od_cf = new long[unigramIters.length-1];
    uw_cf = new long[unigramIters.length-1];

    term_bg = new double[unigramIters.length];
    od_bg = new double[unigramIters.length-1];
    uw_bg = new double[unigramIters.length-1];

    // note that 'this' is a disjunction iterator
    while(!this.isDone()) {
      ctxt.document = this.currentCandidate();

      // collect extent arrays
      ExtentArray pos[] = new ExtentArray[unigramIters.length];
      for (int i = 0; i < unigramIters.length; i++) {
        ExtentIterator unigramIter = unigramIters[i];
        term_cf[i] += unigramIter.count(ctxt);
        pos[i] = unigramIter.extents(ctxt);
      }

      for (int i = 0; i < unigramIters.length - 1; i++) {
        ExtentArray left = pos[i];
        ExtentArray right = pos[i+1];
        od_cf[i] += orderedWindow(left, right);
        uw_cf[i] += unorderedWindow(left, right, 8);
      }
      this.movePast(ctxt.document);
    }

    for (int i = 0; i < unigramIters.length; i++) {
      double cfv = Math.min(0.5, term_cf[i]);
      term_bg[i] = cfv / collectionLength;
    }
    for (int i = 0; i < unigramIters.length - 1; i++) {
      double od_cfv = Math.min(0.5, od_cf[i]);
      od_bg[i] = od_cfv / collectionLength;

      double uw_cfv = Math.min(0.5, uw_cf[i]);
      uw_bg[i] = uw_cfv / collectionLength;
    }

    // reset so we can re-use them
    this.reset();
  }

  private long unorderedWindow(ExtentArray left, ExtentArray right, final int width) {
    ExtentArrayIterator iterA = new ExtentArrayIterator(left);
    ExtentArrayIterator iterB = new ExtentArrayIterator(right);

    if(iterA.isDone() || iterB.isDone()) {
      return 0;
    }

    long count = 0;
    boolean hasNext = true;
    while(hasNext) {
      // choose minimum iterator based on start
      final ExtentArrayIterator minIter = (iterA.currentBegin() < iterB.currentBegin()) ? iterA : iterB;
      final int minimumPosition = minIter.currentBegin();
      final int maximumPosition = Math.max(iterA.currentEnd(), iterB.currentEnd());

      // check for a match
      if(maximumPosition - minimumPosition <= width) {
        //extentCache.add(minimumPosition, maximumPosition);
        count++;
      }

      // move minimum iterator
      hasNext = minIter.next();
    }
    return count;
  }

  private long orderedWindow(ExtentArray leftTerm, ExtentArray rightTerm) {
    final ExtentArrayIterator left = new ExtentArrayIterator(leftTerm);
    final ExtentArrayIterator right = new ExtentArrayIterator(rightTerm);

    // redundant?
    if(left.isDone() || right.isDone())
      return 0;

    long count = 0;
    boolean hasNext = true;
    while(hasNext) {
      final int lhs = left.currentEnd();
      final int rhs = right.currentBegin();

      if(lhs < rhs) {
        hasNext = left.next();
      } else if(lhs > rhs) {
        hasNext = right.next();
      } else { // equal; matched
        count++;
        hasNext = left.next();
      }
    }
    return count;
  }

  @Override
  public double score(ScoringContext c) {
    long length = lengthsIter.length(c);

    double unigram = 0.0;
    // collect extent arrays
    for (int i = 0; i < unigramIters.length; i++) {
      ExtentIterator unigramIter = unigramIters[i];
      pos[i] = unigramIter.extents(c);

      long term_tf = unigramIter.count(c);
      unigram += dirichlet(term_tf, length, mu, term_bg[i]);
    }

    double bigram = 0.0;
    double ubigram = 0.0;

    for (int i = 0; i < unigramIters.length - 1; i++) {
      ExtentArray left = pos[i];
      ExtentArray right = pos[i+1];
      long od_tf = orderedWindow(left, right);
      long uw_tf = unorderedWindow(left, right, 8);

      bigram += dirichlet(od_tf, length, mu, od_bg[i]);
      ubigram += dirichlet(uw_tf, length, mu, uw_bg[i]);
    }

    return unigram * unigramWeight + bigram * bigramWeight + ubigram * ubigramWeight;
  }

  public static double dirichlet(long count, long length, double mu, double background) {
    double numerator = count + mu * background;
    double denominator = length + mu;
    return Math.log(numerator / denominator);
  }

  @Override
  public double maximumScore() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double minimumScore() {
    return Double.NEGATIVE_INFINITY;
  }

  @Override
  public String getValueString(ScoringContext sc) throws IOException {
    return null;
  }

  @Override
  public AnnotatedNode getAnnotatedNode(ScoringContext sc) throws IOException {
    return null;
  }
}

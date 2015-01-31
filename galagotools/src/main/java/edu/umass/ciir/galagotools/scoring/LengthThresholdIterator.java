package edu.umass.ciir.galagotools.scoring;

import org.lemurproject.galago.core.retrieval.iterator.*;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.retrieval.query.AnnotatedNode;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;

/**
 * @author jfoley.
 */
public class LengthThresholdIterator extends TransformIterator implements IndicatorIterator {
  private final int minLength;
  private final LengthsIterator lengths;
  private final int maxLength;
  private ScoringContext fakeScoringContext;

  public LengthThresholdIterator(NodeParameters np, LengthsIterator lengths) throws IOException {
    super(lengths);
    this.lengths = lengths;
    this.minLength = (int) np.get("minLength", np.get("default", 2));
    this.maxLength = (int) np.get("maxLength", Integer.MAX_VALUE);
    this.fakeScoringContext = new ScoringContext();
  }

  @Override
  public boolean indicator(ScoringContext c) {
    int length = lengths.length(c);
    return length >= minLength && length <= maxLength;
  }

  @Override
  public boolean hasAllCandidates() {
    return false;
  }

  @Override
  public boolean hasMatch(long id) {
    if(!lengths.hasMatch(id))
      return false;
    fakeScoringContext.document = id;
    return indicator(fakeScoringContext);
  }

  @Override
  public AnnotatedNode getAnnotatedNode(ScoringContext sc) throws IOException {
    return null;
  }

  public static void addTo(Parameters argp) {
    IterUtils.addToParameters(argp, "lenthresh", LengthThresholdIterator.class);
  }
}


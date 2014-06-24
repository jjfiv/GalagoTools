package edu.umass.ciir.galagotools.scoring;

import gnu.trove.list.array.TDoubleArrayList;
import org.lemurproject.galago.core.index.stats.NodeAggregateIterator;
import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.retrieval.*;
import org.lemurproject.galago.core.retrieval.iterator.CountIterator;
import org.lemurproject.galago.core.retrieval.iterator.DisjunctionIterator;
import org.lemurproject.galago.core.retrieval.iterator.LengthsIterator;
import org.lemurproject.galago.core.retrieval.iterator.ScoreIterator;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.retrieval.query.AnnotatedNode;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.NodeParameters;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.IOException;

/**
 * @author jfoley.
 */
@RequiredStatistics(statistics = {"collectionLength"})
@RequiredParameters(parameters = {"mu"})
public class QueryLikelihood extends DisjunctionIterator implements ScoreIterator {
  private final CountIterator[] iters;
  private final double mu;
  private final TDoubleArrayList backgrounds;
  private final TDoubleArrayList weights;
  private double maxScore;
  private double minScore;
  private LengthsIterator lengthsIter;

  public QueryLikelihood(NodeParameters parameters, LengthsIterator ls, CountIterator[] childIterators) throws IOException {
    super(childIterators);

    this.iters = childIterators;
    this.lengthsIter = ls;

    double collectionLength = parameters.getLong("collectionLength");
    this.mu = parameters.get("mu", 2500.0);

    this.weights = new TDoubleArrayList(iters.length);
    for(int i=0; i<iters.length; i++) {
      weights.add(parameters.get(Integer.toString(i), 1.0));
    }

    // calculate statistics (first pass)
    this.backgrounds = new TDoubleArrayList(iters.length);

    this.maxScore = 0.0;
    this.minScore = 0.0;

    for (int i = 0; i < iters.length; i++) {
      CountIterator iter = iters[i];
      NodeStatistics ns = getNodeFrequency(iter);
      long cf = ns.nodeFrequency;

      double cfv = Math.min(0.5, cf);
      double background = cfv / collectionLength;
      backgrounds.add(background);

      long maxCount = ns.maximumCount;
      double max = dirichlet(maxCount, maxCount, mu, background); // occurs all in 1 doc
      double min = dirichlet(0, 1, mu, background); // tf=0, length=1
      double weight = weights.get(i);

      maxScore += weight*max;
      minScore += weight*min;
    }


  }

  @Override
  public void syncTo(long document) throws IOException {
    super.syncTo(document);
    this.lengthsIter.syncTo(document);
  }

  public static NodeStatistics getNodeFrequency(CountIterator iter) throws IOException {
    if(iter instanceof NodeAggregateIterator) {
      return ((NodeAggregateIterator) iter).getStatistics();
    }

    long cf = 0;
    long maxTF = 0;
    long df = 0;

    ScoringContext ctxt = new ScoringContext();
    while(!iter.isDone()) {
      ctxt.document = iter.currentCandidate();
      if(iter.hasMatch(ctxt.document)) {
        int count = iter.count(ctxt);
        cf += count;
        if (maxTF < count) maxTF = count;
        if(count > 0) df++;
      }
      iter.movePast(ctxt.document);
    }

    NodeStatistics ns = new NodeStatistics();
    ns.maximumCount = maxTF;
    ns.nodeFrequency = cf;
    ns.nodeDocumentCount = df;

    iter.reset();
    return ns;
  }

  public static double dirichlet(long count, long length, double mu, double background) {
    double numerator = count + mu * background;
    double denominator = length + mu;
    return Math.log(numerator / denominator);
  }

  @Override
  public double score(ScoringContext c) {
    double score = 0;
    int length = lengthsIter.length(c);
    for(int i=0; i<iters.length; i++) {
      score += dirichlet(iters[i].count(c), length, mu, backgrounds.get(i)) * weights.get(i);
    }
    return score;
  }

  @Override
  public double maximumScore() {
    return maxScore;
  }

  @Override
  public double minimumScore() {
    return minScore;
  }

  @Override
  public String getValueString(ScoringContext sc) throws IOException {
    return null;
  }

  @Override
  public AnnotatedNode getAnnotatedNode(ScoringContext sc) throws IOException {
    return null;
  }

  public static void executeAndTime(Retrieval ret, String op, String words) throws Exception {
    Node raw = StructuredQuery.parse(String.format("#%s( %s )", op, words));
    Parameters qp = new Parameters();
    //qp.put("processingModel", RankedDocumentModel.class.getName());
    qp.put("fast", true);

    long begin = System.currentTimeMillis();
    Node xquery = ret.transformQuery(raw, qp);
    long xform = System.currentTimeMillis();
    Results res = ret.executeQuery(xquery, qp);
    long end = System.currentTimeMillis();

    System.out.println(op+" XFORM:"+(xform-begin));
    System.out.println(op+" SCORE:"+(end-xform));
    System.out.println(op+res.processingModel);
    System.out.println();
  }

  public static void main(String[] args) throws Exception {
    Parameters argp = Parameters.parseArgs(args);
    IterUtils.addToParameters(argp, "ql", QueryLikelihood.class);
    IterUtils.addToParameters(argp, "flatsdm", FlatSDM.class);
    Retrieval ret = new LocalRetrieval(argp.getString("index"), argp);


    //executeAndTime(ret, "combine", "to be or not to be the very one that we all know");
    //executeAndTime(ret, "ql", "to be or not to be the very one that we all know");
    //executeAndTime(ret, "sdm", "to be or not to be the very one that we all know");
    executeAndTime(ret, "flatsdm", "to be or not to be the very one that we all know");
  }
}

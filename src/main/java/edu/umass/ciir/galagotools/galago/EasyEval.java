package edu.umass.ciir.galagotools.galago;

import org.lemurproject.galago.core.eval.QueryJudgments;
import org.lemurproject.galago.core.eval.QueryResults;
import org.lemurproject.galago.core.eval.QuerySetJudgments;
import org.lemurproject.galago.core.eval.metric.QueryEvaluator;
import org.lemurproject.galago.core.eval.metric.QueryEvaluatorFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.Parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public class EasyEval {
  public static Map<String,Double> eval(QuerySetJudgments qsj, String qid, List<ScoredDocument> docs, List<String> metrics) {
    HashMap<String,Double> metricValues = new HashMap<String,Double>();
    assert(qsj.containsKey(qid));
    assert(!metrics.isEmpty());
    final QueryJudgments qj = qsj.get(qid);
    final QueryResults qres = new QueryResults(qid, docs);
    for(String metric : metrics) {
      QueryEvaluator qeval = QueryEvaluatorFactory.instance(metric, Parameters.instance());
      double score = qeval.evaluate(qres, qj);
      metricValues.put(metric, score);
    }
    return metricValues;
  }

  public static double singleQuery(List<ScoredDocument> results, String qid, QuerySetJudgments qrels, String metric) {
    QueryResults res = new QueryResults(qid, results);
    QueryEvaluator evaluator = QueryEvaluatorFactory.instance(metric, Parameters.instance());
    return evaluator.evaluate(res, qrels.get(qid));
  }
}

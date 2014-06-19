package edu.umass.ciir.galagotools.galago;

import edu.umass.ciir.galagotools.utils.DateUtil;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.util.WordLists;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.IOException;
import java.util.*;

/**
 * @author jfoley
 */
public class QueryUtil {
  public static final Set<String> customStop = new TreeSet<String>(Arrays.asList(new String[]{
    "b", "d", "c", // born, died, circa
    "\u2013", // &ndash;
  }));
  private static Set<String> stopwords;

  public static Set<String> getStopwords() {
    if(stopwords == null) {
      try {
        stopwords = new HashSet<String>();
        stopwords.addAll(WordLists.getWordList("inquery"));
        stopwords.addAll(customStop);
      } catch(IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
    return stopwords;
  }

  public static Node genQuery(Collection<String> terms, String operation) {
    Node op;
    if(operation.equals("combine") || operation.equals("ql")) {
      op = new Node("combine");
    } else if(operation.equals("prox") || operation.equals("sdm")) {
      op = new Node("sdm");
      //op.getNodeParameters().set("default", 20);
    } else {
      throw new IllegalArgumentException("No such queryOperation="+operation);
    }
    for(String term : terms) {
      op.addChild(Node.Text(term));
    }
    //Node scorer = new Node("dirichlet");
    //scorer.addChild(op);
    //return scorer;
    return op;
  }

  public static List<String> filterTerms(Parameters config, List<String> terms) {
    boolean stop = config.get("stopQueries", true);
    boolean removeDates = config.get("stopDates", true);

    ArrayList<String> resultTerms = new ArrayList<String>(terms.size());
    for(String term : terms) {
      if(keepTerm(term, stop, removeDates))
        resultTerms.add(term);
    }
    return resultTerms;
  }

  public static boolean keepTerm(String term, boolean stop, boolean removeDates) {
    if(stop && getStopwords().contains(term))
      return false;
    if(removeDates && (DateUtil.isMonth(term) || DateUtil.isYear(term)))
      return false;
    return true;
  }
}

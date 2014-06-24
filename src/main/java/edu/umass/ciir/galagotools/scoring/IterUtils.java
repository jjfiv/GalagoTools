package edu.umass.ciir.galagotools.scoring;

import org.lemurproject.galago.core.retrieval.iterator.BaseIterator;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 * @author jfoley.
 */
public class IterUtils {
  public static void addToParameters(Parameters p, String name, Class<? extends BaseIterator> iterClass) {
    if(!p.containsKey("operators")) {
      p.put("operators", new Parameters());
    }
    p.getMap("operators").put(name, iterClass.getName());
  }
}

package edu.umass.ciir.galagotools.scoring;

import org.lemurproject.galago.core.retrieval.iterator.BaseIterator;
import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class IterUtils {
  public static void addToParameters(Parameters p, String name, Class<? extends BaseIterator> iterClass) {
    if(!p.containsKey("operators")) {
      p.put("operators", Parameters.instance());
    }
    p.getMap("operators").put(name, iterClass.getName());
  }
}

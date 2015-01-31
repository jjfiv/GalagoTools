package edu.umass.ciir.galagotools.scoring;

import edu.umass.ciir.galagotools.utils.Util;
import org.lemurproject.galago.core.retrieval.iterator.BaseIterator;
import org.lemurproject.galago.core.retrieval.traversal.Traversal;
import org.lemurproject.galago.utility.Parameters;

/**
 * @author jfoley.
 */
public class IterUtils {
  public static void addToParameters(Parameters p, String name, Class<? extends BaseIterator> iterClass) {
    if(!p.containsKey("operators")) {
      p.put("operators", Parameters.create());
    }
    p.getMap("operators").put(name, iterClass.getName());
  }

  public static void addToParameters(Parameters argp, Class<? extends Traversal> traversalClass) {
    Util.extendList(argp, "traversals", Parameters.class, Parameters.parseArray(
      "name", traversalClass.getName(),
      "order", "before"
    ));
  }
}

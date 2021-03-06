package edu.umass.ciir.galagotools.fns;

import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.tools.AppFunction;

import java.util.Collection;

/**
 * @author jfoley.
 */
public final class AppFnRunner {
  public static void main(String[] args, Collection<AppFunction> fns) throws Exception {
    Parameters argp = Parameters.parseArgs(args);
    String whichFn = argp.getString("fn");

    for(AppFunction fn : fns) {
      if(whichFn.equals(fn.getName())) {
        fn.run(argp, System.out);
        return;
      }
    }

    System.err.println("Failed to find AppFunction for fn="+whichFn+" try one of:\n");
    for(AppFunction fn : fns) {
      System.err.println("--fn=" + fn.getName());
    }
  }

  @Deprecated
  public static String helpDescriptions(AppFunction fn, Parameters desc) {
    StringBuilder out = new StringBuilder();
    out.append(fn.getName()).append("\n\n");
    for (String key : desc.keySet()) {
      out.append("\t--").append(key)
          .append('=').append(desc.getAsString(key));
    }
    return out.toString();
  }
}

package edu.umass.ciir.galagotools;

import edu.umass.ciir.galagotools.fns.*;
import edu.umass.ciir.galagotools.spelling.SpellingExpandIndex;
import org.lemurproject.galago.core.tools.App;
import org.lemurproject.galago.utility.tools.AppFunction;

public class Main {
  static AppFunction fns[] = new AppFunction[] {
      new SpellingExpandIndex(),
      new RandomlySampleDocuments(),
      new FindInterestingTerms(),
      new AddMetadataPart()
  };

  public static void main(String[] args) throws Exception {
    for (AppFunction fn : fns) {
      App.appFunctions.put(fn.getName(), fn);
    }
    AppFnRunner.main(args, App.appFunctions.values());
  }
}

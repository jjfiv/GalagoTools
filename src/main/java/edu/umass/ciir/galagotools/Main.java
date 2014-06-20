package edu.umass.ciir.galagotools;

import edu.umass.ciir.galagotools.fns.AddMetadataPart;
import edu.umass.ciir.galagotools.fns.AppFnRunner;
import edu.umass.ciir.galagotools.fns.FindInterestingTerms;
import edu.umass.ciir.galagotools.fns.RandomlySampleDocuments;
import org.lemurproject.galago.core.tools.App;
import org.lemurproject.galago.core.tools.AppFunction;

public class Main {
  static AppFunction fns[] = new AppFunction[] {
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

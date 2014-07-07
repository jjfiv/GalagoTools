package edu.umass.ciir.galagotools;

import edu.umass.ciir.galagotools.fns.*;
import org.lemurproject.galago.core.tools.App;
import org.lemurproject.galago.core.tools.AppFunction;

public class Main {
  static AppFunction fns[] = new AppFunction[] {
      new RandomlySampleDocuments(),
      new FindInterestingTerms(),
      new TarToZipConverter(),
      new AddMetadataPart()
  };

  public static void main(String[] args) throws Exception {
    for (AppFunction fn : fns) {
      App.appFunctions.put(fn.getName(), fn);
    }
    AppFnRunner.main(args, App.appFunctions.values());
  }
}

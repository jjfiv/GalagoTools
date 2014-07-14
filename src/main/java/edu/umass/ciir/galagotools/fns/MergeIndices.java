package edu.umass.ciir.galagotools.fns;

import edu.umass.ciir.galagotools.galago.GalagoUtil;
import edu.umass.ciir.galagotools.utils.IO;
import edu.umass.ciir.galagotools.utils.Util;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.utility.Parameters;

import java.io.PrintWriter;
import java.util.*;

/**
 * @author jfoley
 */
public class MergeIndices {
  public static void main(String[] args) throws Exception {
    Parameters argp = Parameters.parseArgs(args);

    List<String> paths = Arrays.asList(
      "/home/jfoley/code/aladdin2014/data/med14/ucf12.galago/",
      "/home/jfoley/code/aladdin2014/data/med14/ucf13.galago/",
      "/home/jfoley/code/aladdin2014/data/med14/ucf101.galago/",
      "/home/jfoley/code/aladdin2014/data/med14/sar12.galago/",
      "/home/jfoley/code/aladdin2014/data/med14/sarma13.galago/",
      "/home/jfoley/code/aladdin2014/data/med14/sarau13.galago/"
      );
    List<Retrieval> retrievals = new ArrayList<Retrieval>();
    for (String path : paths) {
      retrievals.add(new LocalRetrieval(path));
    }

    Set<String> unionOfNames = new HashSet<String>();

    for (Retrieval retrieval : retrievals) {
      List<String> names = GalagoUtil.names(retrieval);
      System.err.println("# count: "+names.size());
      unionOfNames.addAll(names);
    }

    System.err.println("# total-count: " + unionOfNames.size());

    PrintWriter out = IO.printWriter("all-concepts.docjson.gz"); //argp.getString("output"));

    List<String> allNames = Util.sorted(unionOfNames);
    for (List<String> strings : Util.batched(allNames, 200)) {
      System.err.println("# batch!");
      Map<String,Parameters> docs = new HashMap<String, Parameters>(200);
      for (Retrieval retrieval : retrievals) {
        for (Document document : retrieval.getDocuments(strings, Document.DocumentComponents.All).values()) {
          Parameters forDoc = docs.get(document.name);
          if(forDoc == null) {
            forDoc = Parameters.instance();
            forDoc.put("text", "");
            forDoc.put("name", document.name);
            forDoc.put("meta", Parameters.instance());
            docs.put(document.name, forDoc);
          }
          forDoc.put("text", forDoc.getString("text") + document.text);
          for (String key : document.metadata.keySet()) {
            forDoc.getMap("meta").put(key, document.metadata.get(key));
          }
        }
      }
      for (Parameters docjson : docs.values()) {
        out.println(docjson.toString());
      }
      out.flush();
    }
    out.close();

  }
}

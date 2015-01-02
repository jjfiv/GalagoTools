package edu.umass.ciir.galagotools.fns;

import edu.umass.ciir.galagotools.galago.GalagoUtil;
import edu.umass.ciir.galagotools.utils.IO;
import edu.umass.ciir.galagotools.utils.RandUtil;
import edu.umass.ciir.galagotools.utils.Util;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.utility.Parameters;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

/**
 * @author jfoley.
 */
public class RandomlySampleDocuments extends AppFunction {
  @Override
  public String getName() {
    return "randomly-sample-documents";
  }

  @Override
  public String getHelpString() {
    return makeHelpStr(
        "index", "Galago Index",
        "n", "number of documents to sample",
        "output", "the output file to write (try out.docjson.gz)"
    );
  }

  @Override
  public void run(Parameters p, PrintStream output) throws Exception {
    String indexPath = p.getString("index");
    String outputPath = p.getString("output");
    final int numDocs = (int) p.getLong("n");

    final Random rand = new Random(13);

    DiskIndex index = new DiskIndex(indexPath);
    List<String> names = RandUtil.sampleRandomly(GalagoUtil.asIterable(index.getNamesIterator()), numDocs, rand);

    PrintWriter out = IO.printWriter(outputPath);

    for(List<String> sublist : Util.batched(names, 16)) {
      for (Document doc : index.getDocuments(sublist, Document.DocumentComponents.All).values()) {
        out.println(Parameters.parseArray(
            "name", doc.name,
            "meta", Parameters.parseMap(doc.metadata),
            "text", doc.text
        ));
      }
    }

    out.close();

  }
}

package edu.umass.ciir.galagotools.fns;

import edu.umass.ciir.galagotools.parser.JSONDocParser;
import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.index.corpus.DocumentReader;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.core.tools.apps.BuildIndex;
import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.utility.Parameters;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author jfoley.
 */
public class SampleCorpus extends AppFunction {

  @Override
  public String getName() {
    return "sample-corpus";
  }

  @Override
  public String getHelpString() {
    return makeHelpStr(
      "output", "json lines document",
      "input", "an index with a corpus part",
      "length", "default=8k to take from front of index"
    );
  }

  @Override
  public void run(Parameters argp, PrintStream stdout) throws Exception {
    File output = new File(argp.getString("output"));
    File input = new File(argp.getString("input"));
    if(!input.isDirectory()) throw new IllegalArgumentException("Not a directory!");

    DiskIndex index = new DiskIndex(input.getAbsolutePath());
    CorpusReader reader = (CorpusReader) index.getIndexPart("corpus");
    DocumentReader.DocumentIterator iter = reader.getIterator();

    Document.DocumentComponents docOpts = new Document.DocumentComponents();
    docOpts.subTextStart = 0;
    docOpts.subTextLen = (int) argp.get("length", 8 << 10); // 8k default
    docOpts.text = true;
    docOpts.metadata = true;
    docOpts.tokenize = true;

    File temp = FileUtility.createTemporary();
    stdout.println("Outputting intermediate JSON to "+temp.getAbsolutePath());
    PrintWriter pw = new PrintWriter(temp);

    while(!iter.isDone()) {
      Document doc = iter.getDocument(docOpts);

      if(doc != null) {
        pw.println(Parameters.parseArray(
            "name", doc.name,
            "text", Utility.join(doc.terms, " ")
        ));
      }

      iter.nextKey();
    }
    pw.close();

    Parameters buildP = argp.clone();
    buildP.put("filetype", JSONDocParser.class.getName());
    buildP.put("inputPath", temp.getAbsolutePath());
    buildP.put("indexPath", output.getAbsolutePath());

    BuildIndex.execute(buildP, System.out);
    //stdout.println("Cleaned up temporary file:"+temp.delete());
  }
}

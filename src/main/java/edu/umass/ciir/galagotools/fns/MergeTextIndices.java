package edu.umass.ciir.galagotools.fns;

import edu.umass.ciir.galagotools.fns.AppFnRunner;
import edu.umass.ciir.galagotools.galago.GalagoUtil;
import edu.umass.ciir.galagotools.utils.IO;
import edu.umass.ciir.jasmine.tupleflow.NameFieldText;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.tupleflow.Processor;
import org.lemurproject.galago.tupleflow.Sorter;
import org.lemurproject.galago.tupleflow.error.IncompatibleProcessorException;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author jfoley
 */
public class MergeTextIndices extends AppFunction {
  @Override
  public String getName() {
    return "merge-text-indices";
  }

  @Override
  public String getHelpString() {
    return AppFnRunner.helpDescriptions(this,
      Parameters.parseArray(
        "input", "map of indices to merge; e.g. --input/asr=asr.galago --input/ocr=ocr.galago",
        "output", "json per line documents to write out"
      ));
  }

  public static class NFTDocumentWriter implements NameFieldText.NameFieldOrder.ShreddedProcessor {
    String name;
    String field;
    StringBuilder text;
    PrintStream out;

    public NFTDocumentWriter(String output) throws IOException {
      this.out = IO.printStream(output);
    }

    @Override
    public void processName(String name) throws IOException {
      flush();
      this.name = name;
      this.text = new StringBuilder();
    }

    @Override
    public void processField(String field) throws IOException {
      this.field = field;
    }

    @Override
    public void processTuple(String text) throws IOException {
      this.text.append("<").append(field).append(">\n")
        .append(text)
        .append("\n</").append(field).append(">\n");
    }

    public void flush() {
      if(name == null || text.length() == 0) {
        return;
      }
      Parameters doc = Parameters.instance();
      doc.put("text", text.toString());
      doc.put("name", name);
      out.println(doc.toString());
    }

    @Override
    public void close() throws IOException {
      flush();
      out.close();
    }
  }


  @Override
  public void run(Parameters argp, PrintStream stdout) throws Exception {
    Parameters inputs = argp.getMap("input");

    // tiny pipeline
    try (Processor<NameFieldText> writer = textAggregator(argp.getString("output"))) {
      for (String kind : inputs.keySet()) {
        for (Document doc : GalagoUtil.documentIterable(new DiskIndex(inputs.getString(kind)), Document.DocumentComponents.JustText)) {
          NameFieldText nft = new NameFieldText();
          nft.name = doc.name;
          nft.field = kind;
          nft.text = doc.text;
          writer.process(nft);
        }
      }
    } // close pipeline
    System.err.println("# Done!");
  }

  public static Processor<NameFieldText> textAggregator(String outputFile) throws IOException, IncompatibleProcessorException {
    Sorter<NameFieldText> sorter = new Sorter<>(new NameFieldText.NameFieldOrder());
    sorter.setProcessor(new NameFieldText.NameFieldOrder.TupleShredder(new NFTDocumentWriter(outputFile)));
    return sorter;
  }
}

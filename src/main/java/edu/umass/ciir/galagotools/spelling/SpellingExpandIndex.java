package edu.umass.ciir.galagotools.spelling;

import edu.umass.ciir.galagotools.fns.AppFnRunner;
import edu.umass.ciir.galagotools.fns.MergeTextIndices;
import edu.umass.ciir.jasmine.tupleflow.NameFieldText;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.index.disk.PositionIndexCountSource;
import org.lemurproject.galago.core.index.disk.PositionIndexReader;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.iterator.DataIterator;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.tupleflow.Processor;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * @author jfoley
 */
public class SpellingExpandIndex extends AppFunction {
  @Override
  public String getName() {
    return "spelling-expand-index";
  }

  @Override
  public String getHelpString() {
    return AppFnRunner.helpDescriptions(
      this, Parameters.parseArray(
        "depth", "number of expansions to include",
        "index", "index to expand",
        "trigrams", "trigram dictionary index"
      )
    );
  }

  public static TLongObjectHashMap<String> loadNames(DiskIndex index) throws IOException {
    TLongObjectHashMap<String> names = new TLongObjectHashMap<>();
    // read names into memory
    DataIterator<String> namesIterator = index.getNamesIterator();
    ScoringContext ctx = new ScoringContext();
    while(!namesIterator.isDone()) {
      ctx.document = namesIterator.currentCandidate();
      String name = namesIterator.data(ctx);
      names.put(ctx.document, name);
      namesIterator.movePast(ctx.document);
    }
    return names;
  }
  @Override
  public void run(Parameters argp, PrintStream stdout) throws Exception {

    final int depth = (int) argp.get("depth", 1);

    try (
      Processor<NameFieldText> writer = MergeTextIndices.textAggregator(argp.getString("output"));
      DiskIndex ocr = new DiskIndex(argp.getString("index"));
      LocalRetrieval ret = new LocalRetrieval(argp.getString("trigrams"));
    PositionIndexReader postings = (PositionIndexReader) ocr.getIndexPart("postings")
    ) {
      TLongObjectHashMap<String> names = loadNames(ocr);
      CharacterTrigramTokenizer tok = new CharacterTrigramTokenizer();
      PositionIndexReader.KeyIterator iterator = postings.getIterator();
      for (; !iterator.isDone(); iterator.nextKey()) {
        String term = iterator.getKeyString();
        if (term.length() < 3) continue;
        List<String> trigrams = tok.tokenize(term).terms;
        if (trigrams.isEmpty()) continue;

        Node combine = new Node("combine");
        for (String trigram : trigrams) {
          combine.addChild(Node.Text(trigram));
        }

        Parameters qp = Parameters.instance();
        qp.set("requested", depth);
        Node xq = ret.transformQuery(combine, qp);
        List<ScoredDocument> docs = ret.executeQuery(xq, qp).scoredDocuments;
        if (docs.isEmpty()) continue;
        System.out.println(term);
        for (ScoredDocument doc : docs) {
          System.out.println("> " + doc.documentName);
        }

        PositionIndexCountSource counts = iterator.getValueCountSource();
        while(!counts.isDone()) {
          long doc = counts.currentCandidate();
          String docName = names.get(doc);
          for (int i = 0; i < docs.size(); i++) {
            ScoredDocument sdoc = docs.get(i);
            NameFieldText nft = new NameFieldText();
            nft.field = "spell"+i;
            nft.name = docName;
            nft.text = sdoc.documentName + "\n";
            writer.process(nft);
          }
          counts.movePast(doc);
        }


      }
    } // close
  }
}

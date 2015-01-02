package edu.umass.ciir.galagotools.fns;

import edu.umass.ciir.galagotools.utils.DateUtil;
import edu.umass.ciir.galagotools.galago.WeightedTerm;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.tools.AppFunction;
import org.lemurproject.galago.core.util.FixedSizeMinHeap;
import org.lemurproject.galago.core.util.WordLists;
import org.lemurproject.galago.utility.Parameters;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author jfoley
 */
public class FindInterestingTerms extends AppFunction {

  private Set<String> stopWords;

  @Override
  public String getName() {
    return "find-interesting-terms";
  }

  public void unigrams(Document doc, String id, Parameters argp, DiskIndex index) throws Exception {
    TObjectIntHashMap<String> termCounts = new TObjectIntHashMap<String>();
    for(String term : doc.terms) {
      if(stopWords.contains(term)) continue;
      termCounts.adjustOrPutValue(term, 1, 1);
    }

    System.err.println("# counted: "+id);


    FixedSizeMinHeap<WeightedTerm> rankedTerms = new FixedSizeMinHeap<WeightedTerm>(WeightedTerm.class, (int) argp.get("requested", 25), new WeightedTerm.Comparator());

    Retrieval ret = new LocalRetrieval(index);
    for(String term : termCounts.keySet()) {
      double tf = termCounts.get(term);
      if(tf == 1) continue; // drop freq=1 terms
      NodeStatistics nstats = ret.getNodeStatistics(new Node("counts", term));
      double df = nstats.nodeDocumentCount;
      rankedTerms.offer(new WeightedTerm(term, tf / df));
    }

    List<WeightedTerm> wterms = Arrays.asList(rankedTerms.getSortedArray());
    System.err.println("# results: " + id);
    for (WeightedTerm wterm : wterms) {
      System.out.println(wterm.term);
    }
  }

  @Override
  public String getHelpString() {
    return AppFnRunner.helpDescriptions(this, Parameters.parseArray(
        "index", "INDEX",
        "id", "document id"
    ));
  }

  @Override
  public void run(Parameters argp, PrintStream output) throws Exception {
    DiskIndex index = new DiskIndex(argp.getString("index"));
    String id = argp.getAsString("id");
    Document doc = index.getDocument(id, Document.DocumentComponents.All);
    System.err.println("# pulled: "+id);

    stopWords = WordLists.getWordList("inquery");

    TObjectIntHashMap<String> termCounts = new TObjectIntHashMap<String>();
    List<String> terms = doc.terms;
    for (int i = 0; i < terms.size()-1; i++) {
      String[] gram = new String[] {terms.get(i), terms.get(i+1)};
      boolean skip = false;
      for(String g : gram) {
        if(stopWords.contains(g) || DateUtil.isYear(g)) {
          skip = true;
          break;
        }
      }
      if(skip) continue;
      String bigram = gram[0]+" "+gram[1];
      termCounts.adjustOrPutValue(bigram, 1, 1);
    }

    ArrayList<String> ngrams = new ArrayList<String>();
    for(String ngram : termCounts.keySet()) {
      double tf = termCounts.get(ngram);
      if (tf < 10) continue;
      ngrams.add(ngram);
    }

    System.err.println("# counted: "+id+" numTerms: "+termCounts.size()+" kept: "+ngrams.size());

    FixedSizeMinHeap<WeightedTerm> rankedTerms = new FixedSizeMinHeap<>(WeightedTerm.class, (int) argp.get("requested", 25), new WeightedTerm.Comparator());

    Retrieval ret = new LocalRetrieval(index);
    for(String ngram : ngrams) {
      double tf = termCounts.get(ngram);
      /*Node od = new Node("od");
      for(String t : ngram.split(" ")) {
        od.addChild(new Node("extents", t));
      }
      NodeStatistics nstats = ret.getNodeStatistics(od);
      double df = nstats.nodeDocumentCount;
      rankedTerms.offer(new WeightedTerm(ngram, tf / df));*/
      rankedTerms.offer(new WeightedTerm(ngram, tf));
    }

    List<WeightedTerm> wterms = Arrays.asList(rankedTerms.getSortedArray());
    System.err.println("# results: " + id);
    for (WeightedTerm wterm : wterms) {
      System.out.println(wterm.term);
    }
  }
}


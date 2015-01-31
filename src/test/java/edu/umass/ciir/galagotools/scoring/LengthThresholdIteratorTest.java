package edu.umass.ciir.galagotools.scoring;

import org.junit.Before;
import org.junit.Test;
import org.lemurproject.galago.core.index.mem.MemoryIndex;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.Results;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.tokenize.Tokenizer;
import org.lemurproject.galago.utility.Parameters;

import static org.junit.Assert.assertEquals;

public class LengthThresholdIteratorTest {
  private Tokenizer tokenizer = Tokenizer.create(Parameters.create());
  private MemoryIndex memIndex;

  private Document makeDoc(String title, String body) {
    Document d = new Document();
    d.name = title;
    d.text = body;
    tokenizer.tokenize(d);
    return d;
  }

  @Before
  public void beforeTest() throws Exception {
    this.memIndex = new MemoryIndex();
    memIndex.process(makeDoc("doc0", "This is the way to go home."));
    memIndex.process(makeDoc("doc1", "This is ignored."));
    memIndex.process(makeDoc("doc2", "Ignored, too."));
    memIndex.process(makeDoc("doc3", "The only meaningful documents contain the word home."));
  }

  Results doQ(LocalRetrieval ret, String text) throws Exception {
    Parameters qp = Parameters.create();
    Node query = StructuredQuery.parse(text);
    Node xquery = ret.transformQuery(query, qp);
    System.out.println(xquery);
    return ret.executeQuery(xquery, qp);
  }

  @Test
  public void lengthIteratorTest() throws Exception {
    Parameters defIterAsOp = Parameters.parseArray(
      "operators",
      Parameters.parseArray(
        "lenthresh", LengthThresholdIterator.class.getName()
      )
    );

    LocalRetrieval ret = new LocalRetrieval(memIndex, defIterAsOp);

    Results results;
    results = doQ(ret, "#require(#lenthresh:4(#lengths()) #combine(ignored))");
    assertEquals(0, results.scoredDocuments.size());
    results = doQ(ret, "#require(#lenthresh:3(#lengths()) #combine(ignored))");
    assertEquals(1, results.scoredDocuments.size());
  }

}
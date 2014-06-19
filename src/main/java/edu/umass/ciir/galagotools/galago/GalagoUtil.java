package edu.umass.ciir.galagotools.galago;

import org.lemurproject.galago.core.index.KeyIterator;
import org.lemurproject.galago.core.index.corpus.CorpusReaderSource;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.types.DocumentSplit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class GalagoUtil {
  public static interface Operation<T> {
    public void process(T obj) throws IOException;
  }
  public static <T extends KeyIterator> void forEachKey(T keyIter, Operation<T> action) throws IOException {
    while(!keyIter.isDone()) {
      action.process(keyIter);
      keyIter.nextKey();
    }
  }

  public static DocumentSplit split(String path) {
    DocumentSplit split = new DocumentSplit();
    split.fileName = path;
    return split;
  }

  public static void forEachDocument(DocumentStreamParser parser, Operation<Document> action) throws IOException {
    try {
      while (true) {
        Document doc = parser.nextDocument();
        if (doc == null) break;
        action.process(doc);
      }
    } finally {
      parser.close();
    }
  }

  public static void forCorpusDocument(CorpusReaderSource corpusReaderSource, Operation<Document> action) throws IOException {
    while(!corpusReaderSource.isDone()) {
      long curId = corpusReaderSource.currentCandidate();
      Document doc = corpusReaderSource.data(curId);
      action.process(doc);
      corpusReaderSource.movePast(curId);
    }
  }

  public static List<String> names(List<ScoredDocument> documents) {
    ArrayList<String> names = new ArrayList<String>(documents.size());
    for(ScoredDocument sdoc : documents) {
      names.add(sdoc.documentName);
    }
    return names;
  }
}

package edu.umass.ciir.galagotools.galago;

import edu.umass.ciir.galagotools.callback.Operation;
import org.lemurproject.galago.core.index.Index;
import org.lemurproject.galago.core.index.KeyIterator;
import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.index.corpus.CorpusReaderSource;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.index.source.DataSource;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.retrieval.LocalRetrieval;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.iterator.DataIterator;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.types.DocumentSplit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jfoley
 */
public class GalagoUtil {
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

  public static List<String> names(Retrieval ret) throws IOException {
    ArrayList<String> out = new ArrayList<String>();
    assert(ret instanceof LocalRetrieval);
    Index index = ((LocalRetrieval) ret).getIndex();
    int numNames = (int) index.getIndexPart("names").getManifest().getLong("keyCount");
    out.ensureCapacity(numNames);

    for (String name : asIterable(index.getNamesIterator())) {
      out.add(name);
    }
    return out;
  }

  public static List<String> names(List<ScoredDocument> documents) {
    ArrayList<String> names = new ArrayList<String>(documents.size());
    for(ScoredDocument sdoc : documents) {
      names.add(sdoc.documentName);
    }
    return names;
  }

  private static <T> Iterator<T> asIterator(final DataIterator<T> galagoDataIter) {
    final ScoringContext ctx = new ScoringContext();
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return !galagoDataIter.isDone();
      }

      @Override
      public T next() {
        ctx.document = galagoDataIter.currentCandidate();
        T obj = galagoDataIter.data(ctx);
        try {
          galagoDataIter.movePast(ctx.document);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        return obj;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Read-only iterator");
      }
    };
  }

  public static <T> Iterable<T> asIterable(final DataIterator<T> galagoDataIter) {
    return new Iterable<T>() {
      boolean used = false;
      @Override
      public Iterator<T> iterator() {
        if(used) throw new IllegalStateException("Used DataIterator as Iterable twice!");
        used=true;
        return asIterator(galagoDataIter);
      }
    };
  }

  private static <T> Iterator<T> asIterator(final DataSource<T> source) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return !source.isDone();
      }

      @Override
      public T next() {
        try {
          long doc = source.currentCandidate();
          T obj = source.data(doc);
          source.movePast(doc);
          return obj;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Read-only iterator");
      }
    };
  }

  public static <T> Iterable<T> asIterable(final DataSource<T> src) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return asIterator(src);
      }
    };
  }

  public static Iterable<Document> documentIterable(DiskIndex index, Document.DocumentComponents opts) throws IOException {
    CorpusReader corpus = (CorpusReader) index.getIndexPart("corpus");

    CorpusReaderSource source = corpus.getIterator().getSource(opts);
    return asIterable(source);
  }
}


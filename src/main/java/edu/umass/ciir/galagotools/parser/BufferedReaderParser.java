package edu.umass.ciir.galagotools.parser;

import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author jfoley.
 */
public abstract class BufferedReaderParser extends DocumentStreamParser {
  /** share this with implementing classes */
  public BufferedReader reader;
  public Parameters conf;

  public BufferedReaderParser(DocumentSplit split, Parameters p) throws IOException {
    super(split, p);
    this.reader = DocumentStreamParser.getBufferedReader(split);
    this.conf = p;
  }

  @Override
  public void close() throws IOException {
    if(reader != null) {
      reader.close();
      reader = null;
    }
  }
}

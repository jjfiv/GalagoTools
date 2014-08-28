package edu.umass.ciir.galagotools.spelling;

import edu.umass.ciir.galagotools.utils.IO;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.DocumentStreamParser;
import org.lemurproject.galago.core.types.DocumentSplit;
import org.lemurproject.galago.utility.Parameters;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * For parsing /usr/share/dict/words into trigrams
 * @author jfoley
 */
public class WordListParser extends DocumentStreamParser {

  private BufferedReader reader;

  public WordListParser(DocumentSplit split, Parameters p) throws IOException {
    super(split, p);
    this.reader = getBufferedReader(split);
  }

  @Override
  public Document nextDocument() throws IOException {
    if(reader == null) return null;
    while(true) {
      String line = reader.readLine();
      if (line == null) return null;

      // each line becomes both name and text
      Document doc = new Document();
      doc.name = line.toLowerCase().trim();
      if(doc.name.endsWith("s")) continue;
      if(doc.name.length() < 3) continue;
      doc.text = line;
      return doc;
    }
  }

  @Override
  public void close() throws IOException {
    IO.close(reader);
    reader = null;
  }
}

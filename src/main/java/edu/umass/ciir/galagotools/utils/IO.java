package edu.umass.ciir.galagotools.utils;

import org.lemurproject.galago.tupleflow.FileUtility;
import org.lemurproject.galago.tupleflow.StreamCreator;

import javax.xml.stream.*;
import java.io.*;
import java.util.List;

/**
 * @author jfoley
 */
public class IO {
  public interface StringFunctor {
    public void process(String input);
  }

  public static void forEachLine(List<File> files, StringFunctor doWhat) {
    for(File fp : files) {
      forEachLine(fp, doWhat);
    }
  }

  public static void forEachLine(File fp, StringFunctor doWhat) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(StreamCreator.openInputStream(fp)));

      while(true) {
        String line = reader.readLine();
        if(line == null) break;
        doWhat.process(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if(reader != null) try {
        reader.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static BufferedReader fileReader(String path) throws IOException {
    return fileReader(new File(path));
  }

  public static BufferedReader fileReader(File input) throws IOException {
    return new BufferedReader(new InputStreamReader(StreamCreator.openInputStream(input)));
  }

  public static void forEachLineInStr(String input, StringFunctor doWhat) {
    String[] lines = input.split("\n");
    for(String line : lines) {
      doWhat.process(line);
    }
  }

  private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
  private static final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
  static {
    xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
  }

  public static XMLStreamReader openXMLStream(File fp) throws IOException, XMLStreamException {
    return xmlInputFactory.createXMLStreamReader(StreamCreator.openInputStream(fp), "UTF-8");
  }

  public static XMLStreamWriter writeXMLStream(String output) throws IOException, XMLStreamException {
    return xmlOutputFactory.createXMLStreamWriter(StreamCreator.openOutputStream(output), "UTF-8");
  }

  public static PrintWriter printWriter(String output) throws IOException {
    FileUtility.makeParentDirectories(output);
    return new PrintWriter(StreamCreator.openOutputStream(output));
  }

  public static class PeekLineReader implements Closeable {
    private final BufferedReader reader;
    private String current;

    public PeekLineReader(BufferedReader reader) throws IOException {
      this.reader = reader;
      this.current = reader.readLine();
    }

    public String peek() {
      return current;
    }

    public String next() throws IOException {
      if(current == null) return null;
      String last = current;
      current = reader.readLine();
      return last;
    }

    @Override
    public void close() throws IOException {
      reader.close();
    }
  }
}

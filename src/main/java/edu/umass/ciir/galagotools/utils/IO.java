package edu.umass.ciir.galagotools.utils;

import org.lemurproject.galago.utility.FSUtil;
import org.lemurproject.galago.utility.StreamCreator;
import org.lemurproject.galago.utility.StreamUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
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
      close(reader);
    }
  }

  public static void forEachLine(BufferedReader reader, StringFunctor doWhat) {
    try {
      while (true) {
        String line = reader.readLine();
        if (line == null) break;
        doWhat.process(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      close(reader);
    }
  }

  public static String slurp(File path) throws IOException {
    final StringBuilder contents = new StringBuilder();
    forEachLine(path, new StringFunctor() {
      @Override
      public void process(String input) {
        contents.append(input).append('\n');
      }
    });
    return contents.toString();
  }

  public static String slurp(BufferedReader reader) throws IOException {
    StringBuilder sb = new StringBuilder();
    char buffer[] = new char[4096];
    while(true) {
      int amt = reader.read(buffer);
      if(amt <= 0) break;
      sb.append(buffer, 0, amt);
      if(amt < buffer.length) break;
    }
    return sb.toString();
  }

  public static String slurp(String path) throws IOException {
    return slurp(new File(path));
  }

  public static List<String> slurpLines(String path) throws IOException {
    final ArrayList<String> lines = new ArrayList<String>();
    forEachLine(new File(path), new StringFunctor() {
      @Override
      public void process(String input) {
        lines.add(input);
      }
    });
    return lines;
  }

  public static void copyFile(String from, String to) {
    InputStream is = null;
    try {
      is = StreamCreator.realInputStream(from);
      StreamUtil.copyStreamToFile(is, new File(to));
      is = null;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      close(is);
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

  public static PrintWriter printWriter(String output) throws IOException {
    return new PrintWriter(printStream(output));
  }

  public static PrintStream printStream(String output) throws IOException {
    FSUtil.makeParentDirectories(output);
    return new PrintStream(StreamCreator.openOutputStream(output), true, "UTF-8");
  }

  public static InputStream stringStream(String input) {
    return new ByteArrayInputStream(input.getBytes());
  }

  public static BufferedReader stringReader(String input) {
    return new BufferedReader(new StringReader(input));
  }

  /** Make annoying idiom of Java more tolerable */
  public static void close(Closeable obj) {
    try {
      if(obj != null) obj.close();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static void close(XMLStreamReader xml) {
    try {
      if(xml != null) xml.close();
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
  }


  public static File file(String path) {
    return new File(path);
  }
}

package edu.umass.ciir.galagotools.utils;

import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class IOTest {
  @Test
  public void slurpReaderTest() throws IOException {
    String input = "hello world!";
    assertEquals(input, IO.slurp(IO.stringReader("hello world!")));

    Random rand = new Random();
    String atLeast4k = RandUtil.nextString(rand, 5000);
    assertEquals(atLeast4k, IO.slurp(IO.stringReader(atLeast4k)));
  }
}

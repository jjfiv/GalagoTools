package edu.umass.ciir.galagotools.utils.io;

import edu.umass.ciir.galagotools.utils.IO;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PeekLineReaderTest {

  @Test
  public void testPeekLines() throws IOException {
    String inputTest = "1\n2\n3\n4";
    PeekLineReader plr = new PeekLineReader(new BufferedReader(new StringReader(inputTest)));
    assertEquals("1", plr.peek());
    assertEquals("1", plr.peek());
    assertEquals("1", plr.peek());

    assertEquals("1", plr.next());
    assertEquals("2", plr.peek());
    assertEquals("2", plr.next());

    assertEquals("3", plr.next());
    assertEquals("4", plr.peek());
    assertEquals("4", plr.peek());
    assertNotNull(plr.peek());

    assertEquals("4", plr.next());
    assertNull(plr.peek());
    assertNull(plr.next());
    assertNull(plr.peek());
    assertNull(plr.next());

    IO.close(plr);
  }

}
package edu.umass.ciir.galagotools.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jfoley.
 */
public class SGMLTest {

  @Test
  public void testSGMLRemoveTag() {
    String example1 = "<a href=\"this is removed\">this isn't.</a>";
    Assert.assertEquals("this isn't.", SGML.removeTagsLeaveContents(example1));

    String example2 = "<a href=\"this is removed\">this is too.</a>";
    Assert.assertEquals("", SGML.removeTag(example2, "a"));
  }

  @Test
  public void testSGMLGet() {
    String test = "<key1>foo</key1><key2>bar</key2>";
    Assert.assertEquals("foo", SGML.getTagContents(test, "key1"));
    Assert.assertEquals("bar", SGML.getTagContents(test, "key2"));
  }

  @Test
  public void robustDateGet() {
    String input = "<DATE>930331\n</DATE>\n\n";
    SGML.getTagContents(input, "DATE");
  }
}

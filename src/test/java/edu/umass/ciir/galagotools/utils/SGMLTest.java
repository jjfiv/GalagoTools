package edu.umass.ciir.galagotools.utils;

import org.jsoup.nodes.Attributes;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley.
 */
public class SGMLTest {

  @Test
  public void testSGMLRemoveTag() {
    String example1 = "<a href=\"this is removed\">this isn't.</a>";
    assertEquals("this isn't.", SGML.removeTagsLeaveContents(example1));

    String example2 = "<a href=\"this is removed\">this is too.</a>";
    assertEquals("", SGML.removeTag(example2, "a"));
  }

  @Test
  public void makeTagTest() {
    Map<String,String> attrs = new HashMap<String,String>();
    attrs.put("bar", "baz");
    assertEquals("<foo bar=\"baz\"></foo>", SGML.makeTag("foo", attrs, ""));
    assertEquals("<foo bar=\"baz\"> data </foo>", SGML.makeTag("foo", attrs, "data"));
  }

  @Test
  public void testSGMLGet() {
    String test = "<key1>foo</key1><key2>bar</key2>";
    assertEquals("foo", SGML.getTagContents(test, "key1"));
    assertEquals("bar", SGML.getTagContents(test, "key2"));
  }

  @Test
  public void testSGMLTransform() {
    String inputTest = "<ref>body</ref>\n"+
      "Other things.\n"+
      "<ref name=\"body\">asdf</ref>";

    String output = SGML.transformTag(inputTest, "ref", new SGML.TransformTag() {
      @Override
      public String process(Map<String, String> attrs, String body) {
        if(attrs.containsKey("name")) {
          return attrs.get("name");
        }
        return body;
      }
    });

    assertEquals("body\nOther things.\nbody", output);
  }

  @Test
  public void testGetAttrs() {
    Elements tags = SGML.getTag("<tag key=foo key2=\"abcd\">body textual</tag>", "tag");
    assertEquals(1, tags.size());
    assertEquals("tag", tags.get(0).nodeName());
    Attributes attrs = tags.get(0).attributes();
    assertEquals("foo", attrs.get("key"));
    assertEquals("abcd", attrs.get("key2"));
    assertEquals("body textual", tags.get(0).text());
  }

  @Test
  public void robustDateGet() {
    String input = "<DATE>930331\n</DATE>\n\n";
    SGML.getTagContents(input, "DATE");
  }
}

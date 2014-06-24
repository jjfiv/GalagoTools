package edu.umass.ciir.galagotools.galago;

import org.junit.Test;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueryUtilTest {
  @Test
  public void testTermsFromQuery() {
    Node doThings = StructuredQuery.parse("#combine(#sdm(hello world) #syn(foo bar))");

    List<String> terms = QueryUtil.termsFromQuery(doThings);
    System.out.println(terms);

    assertEquals("hello", terms.get(0));
    assertEquals("world", terms.get(1));
    assertEquals("foo", terms.get(2));
    assertEquals("bar", terms.get(3));
  }

  @Test
  public void testGenQuery() {
    Node ql = QueryUtil.genQuery(Arrays.asList("foo", "bar", "baz"), "combine");
    assertEquals("#combine( #text:foo() #text:bar() #text:baz() )", ql.toString());
  }

}
package edu.umass.ciir.galagotools.utils;

import org.junit.Assert;
import org.junit.Test;

import static edu.umass.ciir.galagotools.utils.DateUtil.*;
import static org.junit.Assert.*;

public class DateUtilTest {
  @Test
  public void nearestDecadeTest() {
    Assert.assertEquals("2010", nearestDecade(2013));
    Assert.assertEquals("0", nearestDecade(7));
    Assert.assertEquals("550BC", nearestDecade(YearFromString("559BC")));
    Assert.assertEquals("560BC", nearestDecade(YearFromString("560BC")));
    Assert.assertEquals("0BC", nearestDecade(-1));
    Assert.assertEquals("0BC", nearestDecade(YearFromString("1BC")));
    Assert.assertEquals("0BC", nearestDecade(YearFromString("9BC")));
    Assert.assertEquals("10BC", nearestDecade(YearFromString("10BC")));
    Assert.assertEquals("10BC", nearestDecade(YearFromString("11BC")));
  }

  @Test
  public void testIsMonthDay() throws Exception {
    assertTrue(isMonthDay("January 27"));
    assertTrue(!isMonthDay("January 123427"));
    assertTrue(!isMonth("January 27"));
  }

  @Test
  public void getYearTest() {
    assertEquals(10, YearFromString("10"));
    assertEquals(-10, YearFromString("11BC"));
    assertEquals(1, YearFromString("1"));
    assertEquals(0, YearFromString("1BC"));
    assertEquals(-1, YearFromString("2BC"));

    assertEquals("10", YearToString(10));
    assertEquals("11BC", YearToString(-10));
    assertEquals("1", YearToString(1));
    assertEquals("1BC", YearToString(0));
    assertEquals("2BC", YearToString(-1));
  }

  @Test
  public void fixRobustDatesTest() {
    Assert.assertEquals("1993-03-31", fixRobustDates("930331"));
    Assert.assertEquals("The date is 1993-03-31", fixRobustDates("The date is 930331"));
    Assert.assertEquals("The date is 1993-03-31. Didn't you know?", fixRobustDates("The date is 930331. Didn't you know?"));
  }
}
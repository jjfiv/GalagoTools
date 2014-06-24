package edu.umass.ciir.galagotools.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author jfoley.
*/
public final class Match {
  public Match(Matcher matcher) {
    begin = matcher.start();
    end = matcher.end();
  }
  public final int begin;
  public final int end;

  public static Match find(String input, Pattern pattern) {
    return find(input, pattern, 0);
  }
  public static Match find(String input, Pattern pattern, int start) {
    Matcher match = pattern.matcher(input);
    if(match.find(start)) {
      return new Match(match);
    }
    return null;
  }

  public String get(String parentStr) {
    return parentStr.substring(begin, end);
  }
}

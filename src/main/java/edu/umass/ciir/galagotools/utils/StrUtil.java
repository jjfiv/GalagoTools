package edu.umass.ciir.galagotools.utils;

import java.util.regex.Pattern;

/**
 * @author jfoley.
 */
public class StrUtil {
  public static String removeBetween(String input, String start, String end) {
    StringBuilder text = new StringBuilder();
    int lastPos = 0;
    while(true) {
      int startPos = input.indexOf(start, lastPos);
      if(startPos == -1) break;
      int endPos = input.indexOf(end, startPos+start.length());
      if(endPos == -1) break;
      endPos += end.length();
      text.append(input.substring(lastPos, startPos));
      lastPos = endPos;
    }
    text.append(input.substring(lastPos));
    return text.toString();
  }

  public static String removeBetweenNested(String input, String start, String end) {
    StringBuilder text = new StringBuilder();
    int lastPos = 0;
    while(true) {
      int startPos = input.indexOf(start, lastPos);
      if(startPos == -1) break;
      int endPos = input.indexOf(end, startPos+start.length());
      if(endPos == -1) break;

      // check for nesting; remove largest matching start,end sequence
      while(true) {
        int nextStartPos = input.indexOf(start, startPos + start.length());
        if(nextStartPos == -1 || nextStartPos > endPos) {
          break;
        }
        int nextEndPos = input.indexOf(end, endPos+end.length());
        if(nextEndPos == -1) break;
        endPos = nextEndPos;
      }

      endPos += end.length();
      text.append(input.substring(lastPos, startPos));
      lastPos = endPos;
    }
    text.append(input.substring(lastPos));
    return text.toString();
  }

  /**
   * Calls transform on every string that exists between patterns start and end on input, and returns the result.
   */
  public static String transformBetween(String input, Pattern start, Pattern end, Transform transform) {
    StringBuilder text = new StringBuilder();
    int lastPos = 0;

    boolean hasNested = false;
    while(true) {
      Match startMatch = Match.find(input, start, lastPos);
      if(startMatch == null) break;
      Match endMatch = Match.find(input, end, startMatch.end);
      if(endMatch == null) break;

      // check for nesting; do inner-most computation first
      while(true) {
        Match nextStartMatch = Match.find(input, start, startMatch.end);
        if(nextStartMatch == null || nextStartMatch.begin > endMatch.begin) {
          break;
        }
        hasNested = true;
        startMatch = nextStartMatch;
      }

      text.append(input.substring(lastPos, startMatch.begin));
      text.append(transform.process(input.substring(startMatch.end, endMatch.begin)));
      lastPos = endMatch.end;
    }
    text.append(input.substring(lastPos));

    // go again to grab the outer ones
    if(hasNested) {
      return transformBetween(text.toString(), start, end, transform);
    }
    return text.toString();
  }

  public static String removeBetween(String input, Pattern start, Pattern end) {
    StringBuilder text = new StringBuilder();
    int lastPos = 0;
    while(true) {
      Match startMatch = Match.find(input, start, lastPos);
      if(startMatch == null) break;
      Match endMatch = Match.find(input, end, startMatch.end);
      if(endMatch == null) break;
      text.append(input.substring(lastPos, startMatch.begin));
      lastPos = endMatch.end;
    }
    text.append(input.substring(lastPos));
    return text.toString();
  }

  public static String takeBefore(String input, String delim) {
    int pos = input.indexOf(delim);
    if(pos == -1) {
      return input;
    }
    return input.substring(0, pos);
  }

  public static String takeAfter(String input, String delim) {
    int pos = input.indexOf(delim);
    if(pos == -1) {
      return input;
    }
    return input.substring(pos+delim.length());
  }

  public static String preview(String input, int len) {
    if(input.length() < len) {
      return input;
    } else {
      return input.substring(0, len-2)+"..";
    }
  }

  public static String firstWord(String text) {
    for(int i=0; i<text.length(); i++) {
      if(Character.isWhitespace(text.charAt(i)))
        return text.substring(0,i);
    }
    return text;
  }

  public static boolean looksLikeInt(String str, int numDigits) {
    if(str.isEmpty()) return false;
    if(str.length() > numDigits)
      return false;
    for(char c : str.toCharArray()) {
      if(!Character.isDigit(c))
        return false;
    }
    return true;
  }

  public static String removeSpaces(String input) {
    StringBuilder output = new StringBuilder();
    for(char c : input.toCharArray()) {
      if(Character.isWhitespace(c))
        continue;
      output.append(c);
    }
    return output.toString();
  }

  public static String filterToAscii(String input) {
    StringBuilder ascii = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      if (input.codePointAt(i) <= 127) {
        ascii.append(input.charAt(i));
      }
    }
    return ascii.toString();
  }

  public static boolean isAscii(String input) {
    for (int i = 0; i < input.length(); i++) {
      if (input.codePointAt(i) > 127) {
        return false;
      }
    }
    return true;
  }

  public static String compactSpaces(String input) {
    StringBuilder sb = new StringBuilder();
    boolean lastWasSpace = true;
    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);
      if(Character.isWhitespace(ch)) {
        if(lastWasSpace) continue;
        sb.append(' ');
        lastWasSpace = true;
        continue;
      }
      lastWasSpace = false;
      sb.append(ch);
    }
    return sb.toString();
  }

  public static interface Transform {
    public String process(String input);
  }

}

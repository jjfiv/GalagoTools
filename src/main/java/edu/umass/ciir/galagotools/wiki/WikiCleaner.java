package edu.umass.ciir.galagotools.wiki;

import edu.umass.ciir.galagotools.utils.DateUtil;
import edu.umass.ciir.galagotools.utils.SGML;
import edu.umass.ciir.galagotools.utils.StrUtil;

import java.util.regex.Pattern;

/**
 * @author jfoley.
 */
public class WikiCleaner {
  public static String removeReferences(String input) {
    input = SGML.removeTag(input, "ref");
    input = StrUtil.removeBetweenNested(input, "{{refbegin", "{{refend}}");
    return input;
  }

  public static String cleanWikiTags(String input) {
    return input.replaceAll("</?(onlyinclude|includeonly)>", "");
  }

  public static String killWikiTables(String input) {
    return StrUtil.removeBetweenNested(input, "{|", "|}");
  }

  public static String convertExternalLinks(String input) {
    return StrUtil.transformBetween(input, Pattern.compile("\\["), Pattern.compile("\\]"), new StrUtil.Transform() {
      @Override
      public String process(String input) {
        String url = input;
        String text = "link";
        if(input.contains(" ")) {
          url = StrUtil.takeBefore(input, " ");
          text = StrUtil.takeAfter(input, " ");
        }
        return String.format("<a href=\"%s\">%s</a>", url, text);
      }
    });
  }

  public static String convertInternalLinks(String input) {
    return StrUtil.transformBetween(input, Pattern.compile("\\[\\["), Pattern.compile("\\]\\]"), new StrUtil.Transform() {
      @Override
      public String process(String input) {
        if(input.charAt(0) == ':') { // special category sort of link
          return "";
        } else if(input.startsWith("File:") || input.startsWith("Image:") || input.startsWith("Category:")) {
            return "";
        }

        String url;
        String text;

        if(input.contains("|")) {
          url = StrUtil.takeBefore(input, "|");
          text = StrUtil.takeAfter(input, "|");
        } else {
          url = input;
          text = input;
        }

        if(DateUtil.isMonthDay(text))
          return text;

        return internalLink(url, text);
      }
    });
  }

  public static String internalLink(String page, String text) {
    String url = page.replaceAll("\\s", "_");
    return String.format("<a href=\"https://en.wikipedia.org/wiki/%s\">%s</a>", url, text);
  }

  public static String unescapeAmpersandEscapes(String input) {
    return input.replaceAll("\\&ndash;", "-");
  }

  public static String clean(String input) {
    return clean("test", input);
  }

  public static String clean(String title, String input) {
    input = removeReferences(input);
    input = unescapeAmpersandEscapes(input);
    input = cleanWikiTags(input);
    input = killWikiTables(input);
    input = SGML.removeComments(input);
    input = input.replaceAll("''", ""); // ditch all italics
    input = WikiTemplateHack.convertTemplates(title, input);
    input = convertInternalLinks(input);
    input = convertExternalLinks(input);
    return input;
  }
}

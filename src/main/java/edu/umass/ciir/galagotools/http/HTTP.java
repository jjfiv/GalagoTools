package edu.umass.ciir.galagotools.http;

import javax.servlet.http.HttpServletRequest;

public class HTTP {
  public static int InternalError = 501;
  public static int Success = 200;
  public static int BadRequest = 400;
  public static int NotFound = 404;


  public static String getContentType(HttpServletRequest req) {
    String contentType = req.getContentType();
    if(contentType != null && contentType.contains(";")) {
      return contentType.substring(0, contentType.indexOf(";"));
    }
    return contentType;
  }

  public static boolean isFormData(String contentType) {
    if(contentType == null) return false;
    return "application/x-www-form-urlencoded".equals(contentType);
  }
}

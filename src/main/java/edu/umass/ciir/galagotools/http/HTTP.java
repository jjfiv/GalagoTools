package edu.umass.ciir.galagotools.http;

import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.StreamUtil;
import org.lemurproject.galago.utility.json.JSONUtil;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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


  public static Parameters toJSON(HttpServletRequest req) throws IOException {
    Parameters reqp = Parameters.instance();

    String contentType = req.getContentType();
    // chrome likes to send:
    //   application/x-www-form-urlencoded; charset=UTF-8 len:96
    if(contentType != null && contentType.contains(";")) {
      contentType = contentType.substring(0, contentType.indexOf(";"));
    }

    // GET or POST form parameters handling
    if(contentType == null || "application/x-www-form-urlencoded".equals(contentType)) {
      Map<String, String[]> asMap = (Map<String, String[]>) req.getParameterMap();

      for (Map.Entry<String, String[]> kv : asMap.entrySet()) {
        String arg = kv.getKey();
        String[] values = kv.getValue();

        if (values.length == 1) {
          reqp.put(arg, JSONUtil.parseString(values[0]));
        } else {
          reqp.set(arg, new ArrayList());
          for (String val : values) {
            reqp.getList(arg, Object.class).add(JSONUtil.parseString(val));
          }
        }
      }
      return reqp;
    } else if(contentType.equals("application/json")) {
      // request body as JSON handling
      ServletInputStream sis = req.getInputStream();
      String body = StreamUtil.copyStreamToString(sis);
      sis.close();

      return Parameters.parseString(body);
    } else if(req.getContentLength() > 0) {
      throw new UnsupportedOperationException("Unknown data kind sent to server: "+contentType+" len:"+req.getContentLength());
    }

    return reqp;
  }
}

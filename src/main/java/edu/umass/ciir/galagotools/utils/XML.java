package edu.umass.ciir.galagotools.utils;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jfoley
 */
public class XML {
  public static Map<String,String> getFields(XMLStreamReader xml, String endTag, List<String> fields) throws IOException, XMLStreamException {
    HashMap<String,StringBuilder> builders = new HashMap<String,StringBuilder>();
    for(String field : fields) {
      builders.put(field, new StringBuilder());
    }
    Set<String> keys = builders.keySet();

    // collect all listed tags
    String currentTag = null;
    while (xml.hasNext()) {
      int event = xml.next();

      if(event == XMLStreamConstants.START_ELEMENT) {
        String tagName = xml.getLocalName();
        if(keys.contains(tagName)) {
          currentTag = tagName;
        } else if(currentTag != null) {
          builders.get(currentTag).append("<").append(tagName).append(">");
        }
      } else if(event == XMLStreamConstants.END_ELEMENT) {
        String tagName = xml.getLocalName();
        if (tagName.equals(currentTag)) {
          currentTag = null;
        } else if(tagName.equals(endTag)) {
          break;
        } else if(currentTag != null) {
          builders.get(currentTag).append("</").append(tagName).append(">");
        }
      } else if(event == XMLStreamConstants.CDATA || event == XMLStreamConstants.CHARACTERS) {
        if(currentTag != null) {
          builders.get(currentTag).append(xml.getText());
        }
      }
    }

    // finish off builders
    HashMap<String,String> results = new HashMap<String,String>();
    for(Map.Entry<String,StringBuilder> kv : builders.entrySet()) {
      results.put(kv.getKey(), kv.getValue().toString());
    }
    return results;
  }

  public static interface FieldsFunctor {
    public void process(Map<String,String> fieldValues);
  }

  public static void forFieldsInSections(File fp, String sectionTag, List<String> fields, FieldsFunctor operation) throws IOException, XMLStreamException {
    XMLStreamReader xml = null;
    try {
      xml = IO.openXMLStream(fp);

      while (xml.hasNext()) {
        Map<String,String> data = getFields(xml, sectionTag, fields);
        operation.process(data);
      }
    } finally {
      if(xml != null) xml.close();
    }
  }

}

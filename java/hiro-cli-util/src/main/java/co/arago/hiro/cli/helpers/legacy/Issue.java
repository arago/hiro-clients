package co.arago.hiro.cli.helpers.legacy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static co.arago.hiro.client.api.OGITConstants.Attributes.*;
import static co.arago.hiro.client.api.OGITConstants.Entities.*;
import static co.arago.hiro.client.api.OGITConstants.Verbs.*;

/**
 *
 */
public class Issue {

  public static final boolean XIDS_SUPPORTED = false;
  public static final boolean DEPLOY_SUPPORTED = true;

  public static final List<String> OUTPUT_FORMATS = Arrays.asList("yaml", "json", "json-compact");
  public static final String WRAPPED_FORMAT = "xml";
  public static final String DEF_OUTPUT_FORMAT = "json";
  public static final List<String> INPUT_FORMATS = Arrays.asList("yaml", "xml");
  public static final String DEF_INPUT_FORMAT = "xml";

  public static final String XML_ID_ATTR = "IID";
  public static final String VERTEX_TYPE = AUTOMATION_AUTOMATIONISSUE;
  public static final String VERTEX_TYPE_DISPLAY_NAME = "Issue";
  public static final String ISSUE_VERTEX_STATUS_FIELD = OGIT_STATUS;
  public static final String ISSUE_VERTEX_NODE_FIELD = "/NodeID";
  public static final String HISTORY_VERTEX_TYPE = AUTOMATION_HISTORY;
  public static final String HISTORY_VERTEX_EDGE = OGIT_GENERATES;
  public static final String HISTORY_TIMESTAMP = OGIT_TIMESTAMP;
  public static final String HISTORY_ENTRY_NAME = "/IssueHistory";
  public static final List<String> LONG_LIST_ATTRS = Arrays.asList(OGIT_SUBJECT, OGIT_STATUS, AUTOMATION_IS_DEPLOYED, ISSUE_VERTEX_NODE_FIELD);
  public static final String ISSUE_EXTERNAL_RESOLVE = "RESOLVED_EXTERNAL";
  public static final String ISSUE_RESOLVE = "RESOLVED";

  public static Map toMap(final String xml) throws ParserConfigurationException, SAXException, IOException {
    Map m = new HashMap();
    m.put(AUTOMATION_ISSUEFORMALREPRESENTATION, xml);
    DocumentBuilder builder = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    // support rare case of updating the issue XML
    if (doc.getDocumentElement().hasAttribute(XML_ID_ATTR)) {
      m.put(OGIT__XID, doc.getDocumentElement().getAttribute(XML_ID_ATTR));
    }

    return m;
  }

  // we can return object here. the client will handle it properly
  public static Object unwrap(final String outputFormat, final Map m) {
    if (outputFormat.equals(WRAPPED_FORMAT)) {
      return (String) m.get(AUTOMATION_ISSUEFORMALREPRESENTATION);
    } else {
      return m;
    }
  }
}

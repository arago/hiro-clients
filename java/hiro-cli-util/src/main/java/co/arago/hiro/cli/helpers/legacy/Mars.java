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

/**
 *
 */
public class Mars {

  public static final boolean XIDS_SUPPORTED = true;
  public static final boolean DEPLOY_SUPPORTED = true;

  public static final List<String> OUTPUT_FORMATS = Arrays.asList("xml", "json", "json-compact");
  public static final String WRAPPED_FORMAT = "xml";
  public static final String DEF_OUTPUT_FORMAT = "xml";
  public static final List<String> INPUT_FORMATS = Arrays.asList("xml");
  public static final String DEF_INPUT_FORMAT = "xml";

  public static final String XML_ID_ATTR = "ID";
  public static final String XML_FQDN_ATTR = "FQDN";
  public static final String XML_NODENAME_ATTR = "NodeName";
  public static final String XML_NODETYPE_ATTR = "NodeType";
  public static final String VERTEX_TYPE = AUTOMATION_MARSNODE;
  public static final String VERTEX_TYPE_DISPLAY_NAME = "MARSNode";
  public static final String DECOMMISSIONED_STATE = "decommissioned";
  public static final List<String> LONG_LIST_ATTRS = Arrays.asList(OGIT__XID, OGIT_NAME, AUTOMATION_IS_DEPLOYED, AUTOMATION_MARSNODETYPE, AUTOMATION_LIFECYCLE);

  public static Map toMap(final boolean decommission, final String xml) throws ParserConfigurationException, SAXException, IOException {
    Map m = new HashMap();
    m.put(AUTOMATION_MARSNODEFORMALREPRESENTATION, xml);
    // Note:
    DocumentBuilder builder = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    String id = doc.getDocumentElement().getAttribute(XML_ID_ATTR);
    // we set ogit/_xid to get an 409 even if engine changed ownership such that we may get 403
    m.put(OGIT__XID, id);

    String name = null;
    if (doc.getDocumentElement().hasAttribute(XML_FQDN_ATTR)) {
      name = doc.getDocumentElement().getAttribute(XML_FQDN_ATTR);
    } else {
      if (doc.getDocumentElement().hasAttribute(XML_NODENAME_ATTR)) {
        name = doc.getDocumentElement().getAttribute(XML_NODENAME_ATTR);
      }
    }
    if (name != null && !name.isEmpty()) {
      m.put(OGIT_NAME, name);
    }
    if (doc.getDocumentElement().hasAttribute(XML_NODETYPE_ATTR)) {
      m.put(AUTOMATION_MARSNODETYPE, doc.getDocumentElement().getAttribute(XML_NODETYPE_ATTR));
    }
    if (decommission) {
      m.put(AUTOMATION_LIFECYCLE, DECOMMISSIONED_STATE);
    }
    return m;
  }

  // we can return object here. the client will handle it properly
  public static Object unwrap(final String outputFormat, final Map m) {
    if (outputFormat.equals(WRAPPED_FORMAT)) {
      return (String) m.get(AUTOMATION_MARSNODEFORMALREPRESENTATION);
    } else {
      return m;
    }
  }
}

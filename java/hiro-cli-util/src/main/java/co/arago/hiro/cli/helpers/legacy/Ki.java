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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static co.arago.hiro.client.api.OGITConstants.Attributes.*;
import static co.arago.hiro.client.api.OGITConstants.Entities.*;

/**
 *
 */
public class Ki {

  public static final boolean XIDS_SUPPORTED = true;
  public static final boolean DEPLOY_SUPPORTED = true;

  public static final List<String> OUTPUT_FORMATS = Arrays.asList("xml", "json", "json-compact");
  public static final List<String> WRAPPED_FORMATS = Arrays.asList("xml", "ki"); // "ki" for compat
  public static final String WRAPPED_FORMAT = "ki";
  public static final String DEF_OUTPUT_FORMAT = "xml";
  public static final String COMPAT_OUTPUT_FORMAT = "ki"; // in cases we encounter new style KIs
  public static final List<String> INPUT_FORMATS = Arrays.asList("xml");
  public static final String DEF_INPUT_FORMAT = "xml";

  public static final String XML_ID_ATTR = "ID";
  public static final String XML_NAME_EL = "Title";
  public static final String XML_DESC_EL = "Description";
  public static final String VERTEX_TYPE = AUTOMATION_KNOWLEDGEITEM;
  public static final String VERTEX_TYPE_DISPLAY_NAME = "KI";
  public static final String KI_SYNTAX_VESRION = "1.0";
  public static final String KI_SYNTAX_VESRION_PREFIX = "1.";
  public static final String KI_TEXT_PREFIX = "<";
  public static final List<String> LONG_LIST_ATTRS = Arrays.asList(OGIT__XID, OGIT_NAME, AUTOMATION_KNOWLEDGEITEM_SYNTAXVERSION, OGIT_DESCRIPTION, AUTOMATION_IS_DEPLOYED, AUTOMATION_DEPLOYTOENGINE);

  public static Map toMap(final boolean deploy, final String xml) throws ParserConfigurationException, SAXException, IOException {
    Map m = new HashMap();
    m.put(AUTOMATION_KNOWLEDGEITEMFORMALREPRESENTATION, xml);
    // Note:
    DocumentBuilder builder = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    String id = doc.getDocumentElement().getAttribute(XML_ID_ATTR);
    // we set ogit/_id to get an 409 even if engine changed ownership such that we may get 403
    m.put(OGIT__XID, id);
    m.put(AUTOMATION_KNOWLEDGEITEM_SYNTAXVERSION, KI_SYNTAX_VESRION);

    String name = null;
    NodeList elementsByTagName = doc.getElementsByTagName(XML_NAME_EL);
    if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
      m.put(OGIT_NAME, elementsByTagName.item(0).getTextContent());
    }
    elementsByTagName = doc.getElementsByTagName(XML_DESC_EL);
    if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
      m.put(OGIT_DESCRIPTION, elementsByTagName.item(0).getTextContent());
    }
    m.put(AUTOMATION_DEPLOYTOENGINE, Boolean.toString(deploy));
    return m;
  }

  // we can return object here. the client will handle it properly
  public static Object unwrap(final String outputFormat, final Map m) {
    if (WRAPPED_FORMATS.contains(outputFormat)) {
      return (String) m.get(AUTOMATION_KNOWLEDGEITEMFORMALREPRESENTATION);
    } else {
      return m;
    }
  }

  public static boolean isMatchingKi(final Map m) {
    if (m.containsKey(AUTOMATION_KNOWLEDGEITEM_SYNTAXVERSION)) {
      if (((String) m.get(AUTOMATION_KNOWLEDGEITEM_SYNTAXVERSION)).startsWith(KI_SYNTAX_VESRION_PREFIX)) {
        return true;
      } else {
        return false;
      }
    } else {
      if (((String) m.getOrDefault(AUTOMATION_KNOWLEDGEITEMFORMALREPRESENTATION, "")).startsWith(KI_TEXT_PREFIX)) {
        return true;
      }
      return false;
    }
  }
}

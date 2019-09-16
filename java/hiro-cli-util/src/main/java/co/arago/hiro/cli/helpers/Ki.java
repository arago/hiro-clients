package co.arago.hiro.cli.helpers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

import static co.arago.hiro.client.api.OGITConstants.Attributes.*;
import static co.arago.hiro.client.api.OGITConstants.Entities.*;

public class Ki {

  public static final boolean XIDS_SUPPORTED = true;
  public static final boolean DEPLOY_SUPPORTED = false; // assuming edge-based KI deployment
  public static final List<String> OUTPUT_FORMATS = Arrays.asList("ki", "json", "json-compact");
  public static final List<String> WRAPPED_FORMATS = Arrays.asList("xml", "ki"); // "xml" for compat
  public static final String WRAPPED_FORMAT = "ki";
  public static final String DEF_OUTPUT_FORMAT = "ki";
  public static final String COMPAT_OUTPUT_FORMAT = "xml"; // in cases we encounter old style KIs
  public static final List<String> INPUT_FORMATS = Arrays.asList("ki");
  public static final String DEF_INPUT_FORMAT = "ki";

  public static final String KI_SECTION = "ki";
  public static final String KI_META_ID = "id";
  public static final String KI_META_NAME = "name";
  public static final String KI_META_DESCRIPTION = "description";
  public static final String VERTEX_TYPE = AUTOMATION_KNOWLEDGEITEM;
  public static final String VERTEX_TYPE_DISPLAY_NAME = "KI";
  public static final String KI_SYNTAX_VESRION = "2.0";
  public static final String KI_SYNTAX_VESRION_PREFIX = "2.";
  public static final String KI_TEXT_PREFIX = "ki";
  public static final List<String> LONG_LIST_ATTRS = Arrays.asList(OGIT__XID, OGIT_NAME, AUTOMATION_KNOWLEDGEITEM_SYNTAXVERSION, OGIT_DESCRIPTION);
  public static final List<String> SHORT_LIST_ATTRS = Arrays.asList(OGIT__XID, OGIT_NAME);

  public static Map toMap(final boolean ignore, final String ki) throws IOException {
    Map m = new HashMap();
    m.put(AUTOMATION_KNOWLEDGEITEMFORMALREPRESENTATION, ki);

    Map<String, String> metaData = parseKiMetaData(ki);
    if (metaData.containsKey(KI_META_ID)) {
      m.put(OGIT__XID, metaData.get(KI_META_ID));
    }
    m.put(AUTOMATION_KNOWLEDGEITEM_SYNTAXVERSION, KI_SYNTAX_VESRION);
    if (metaData.containsKey(KI_META_NAME)) {
      m.put(OGIT_NAME, metaData.get(KI_META_NAME));
    }
    if (!m.containsKey(OGIT__XID) && !m.containsKey(OGIT_NAME)) {
      throw new IllegalArgumentException("KI metadata must contain at least one of: " + KI_META_ID + ", " + KI_META_NAME);
    }
    if (metaData.containsKey(KI_META_DESCRIPTION)) {
      m.put(OGIT_DESCRIPTION, metaData.get(KI_META_DESCRIPTION));
    }
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

  public static Map<String, String> parseKiMetaData(String ki) {
    Map<String, String> result = new HashMap<>();

    int begin = ki.indexOf("ki\n");
    int end = ki.indexOf("\non\n");
    if (begin != 0) {
      throw new RuntimeException("KI text must start with 'ki' tag. Parsed KI text: " + ki);
    }
    if (end < 0) {
      throw new RuntimeException("KI text must contain 'on' tag. Parsed KI text: " + ki);
    }
    Yaml parser = new Yaml();
    String toParse = "ki:\n" + ki.substring(begin + "ki\n".length(), end) + "\n";
    Object o = parser.load("ki:\n" + ki.substring(begin + "ki\n".length(), end) + "\n");
    Map m = (Map) ((Map) o).get("ki");
    for (Object key : m.keySet()) {
      result.put((String) key, (String) m.get(key));
    }
    return result;
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

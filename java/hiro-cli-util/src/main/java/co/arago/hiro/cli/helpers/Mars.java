package co.arago.hiro.cli.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.arago.hiro.client.api.OGITConstants.Attributes.*;
import static co.arago.hiro.client.api.OGITConstants.Entities.*;
import static co.arago.hiro.client.api.OGITConstants.Verbs.*;
import static co.arago.hiro.client.api.OGITConstants.NAMESPACE_MARS;
import net.minidev.json.JSONValue;

/**
 *
 */
public class Mars {

  private static final Logger LOG = Logger.getLogger(Mars.class.getName());
  public static final List<String> OUTPUT_FORMATS = Arrays.asList("json", "json-compact");
  public static final String WRAPPED_FORMAT = "NONE";
  public static final String DEF_OUTPUT_FORMAT = "json";
  public static final String MARS_EDGE_TYPE = OGIT_DEPENDS_ON;
  public static final boolean XIDS_SUPPORTED = true;
  public static final boolean DEPLOY_SUPPORTED = false;
  public static final List<String> INPUT_FORMATS = Arrays.asList("json");
  public static final String DEF_INPUT_FORMAT = "json";

  public static final String NODEID_ATTR = OGIT__XID;
  public static final String FQDN_ATTR = MARS_NETWORK_FQDN;
  public static final String NODENAME_ATTR = OGIT_NAME;
  public static final String VERTEX_TYPE = NAMESPACE_MARS + "*";
  public static final String VERTEX_TYPE_DISPLAY_NAME = "MARSNode";
  public static final String DEFAULT_NODE_STATUS = "unknown";
  public static final List<String> ALLOWED_TYPES = Arrays.asList(MARS_APPLICATION, MARS_RESOURCE, MARS_SOFTWARE, MARS_MACHINE);
  public static final List<String> REQUIRED_ATTRS = Arrays.asList(NODEID_ATTR, NODENAME_ATTR, OGIT__TYPE);
  public static final List<String> LONG_LIST_ATTRS = Arrays.asList(OGIT__XID, OGIT__TYPE, OGIT_NAME, AUTOMATION_LIFECYCLE, AUTOMATION_SERVICESTATUS, AUTOMATION_AUTOMATION_STATE);
  public static final List<String> SHORT_LIST_ATTRS = Arrays.asList(OGIT__TYPE, OGIT__XID, OGIT_NAME);

  public static Map toMap(final String json) {
    Object o = JSONValue.parse(json);
    if (!(o instanceof Map)) {
      throw new IllegalArgumentException("input string is not a valid JSON map");
    }
    Map m = (Map) o;
    for (String attr : REQUIRED_ATTRS) {
      if (!m.containsKey(attr)) {
        throw new IllegalArgumentException("Mandatory argument (" + attr + ") missing in JSON input");
      }
    }
    if (m.containsKey(OGIT__ID)) {
      LOG.log(Level.WARNING, "Ingoring \"{0}\"=\"{1}\" from input JSON",
        new Object[]{OGIT__ID, m.get(OGIT__ID)});
      m.remove(OGIT__ID);
    }
    if (m.containsKey(OGIT__TYPE) && !ALLOWED_TYPES.contains(m.get(OGIT__TYPE))) {
      throw new IllegalArgumentException("JSON input defines " + OGIT__TYPE + "="
        + m.get(OGIT__TYPE) + " but only " + VERTEX_TYPE + " is allowed here");
    }
    if (!m.containsKey(AUTOMATION_SERVICESTATUS)) { // for HIRO Cockpit
      m.put(AUTOMATION_SERVICESTATUS, DEFAULT_NODE_STATUS);
    }
    return m;
  }

  // we can return object here. the client will handle it properly
  public static Object unwrap(final String outputFormat, final Map m) {
    return m;
  }
}

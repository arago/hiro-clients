package co.arago.hiro.cli.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.arago.hiro.client.api.OGITConstants.Attributes.*;
import static co.arago.hiro.client.api.OGITConstants.Entities.*;
import static co.arago.hiro.client.api.OGITConstants.Verbs.*;
import net.minidev.json.JSONValue;

/**
 *
 */
public class Issue {

  private static final Logger LOG = Logger.getLogger(Issue.class.getName());
  public static final boolean XIDS_SUPPORTED = false;
  public static final String DEPLOYED_STATUS_VALUE = "deployed";
  public static final List<String> OUTPUT_FORMATS = Arrays.asList("json", "json-compact");
  public static final String WRAPPED_FORMAT = "NONE";
  public static final String DEF_OUTPUT_FORMAT = "json";
  public static final List<String> INPUT_FORMATS = Arrays.asList("json");
  public static final String DEF_INPUT_FORMAT = "json";

  public static final String VERTEX_TYPE = AUTOMATION_AUTOMATIONISSUE;
  public static final String VERTEX_TYPE_DISPLAY_NAME = "Issue";
  public static final String ISSUE_VERTEX_STATUS_FIELD = OGIT_STATUS;
  public static final String ISSUE_VERTEX_NODE_FIELD = AUTOMATION_ISSUE_ORIGIN_NODE;
  public static final String HISTORY_VERTEX_TYPE = OGIT_TIMESERIES;
  public static final String HISTORY_VERTEX_EDGE = OGIT_GENERATES;
  public static final String HISTORY_ENTRY_NAME = "_processing_log";
  public static final List<String> REQUIRED_ATTRS = Arrays.asList(AUTOMATION_ISSUE_ORIGIN_NODE, OGIT_SUBJECT);
  public static final List<String> LONG_LIST_ATTRS = Arrays.asList(OGIT_SUBJECT, OGIT_STATUS, AUTOMATION_ISSUE_ORIGIN_NODE, AUTOMATION_ISSUE_PROCESSING_NODE);
  public static final List<String> SHORT_LIST_ATTRS = Arrays.asList(OGIT_STATUS, OGIT_SUBJECT);
  public static final String ISSUE_NEW = "NEW";
  public static final String ISSUE_RESOLVE = "RESOLVED";
  public static final String ISSUE_NO_RESOLVE = "TERMINATED";
  public static final String ISSUE_PROCESSING = "PROCESSING";
  public static final String ISSUE_WAITING = "WAITING";
  public static final String ISSUE_STOPPED = "STOPPED";
  // compat:
  public static final String ISSUE_NO_RESOLVE_COMPAT = "RESOLVED_EXTERNAL";

  public static List<String> finalStates() {
    return Arrays.asList(ISSUE_RESOLVE,  ISSUE_NO_RESOLVE, ISSUE_NO_RESOLVE_COMPAT);
  }
  
  // we can return object here. the client will handle it properly
  public static Object unwrap(final String outputFormat, final Map m) {
    return m;
  }

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
    if (!m.containsKey(AUTOMATION_ISSUETYPE)) {
      m.put(AUTOMATION_ISSUETYPE, "unknown");
    }
    if (m.containsKey(OGIT__ID)) {
      LOG.log(Level.WARNING, "Ingoring \"{0}\"=\"{1}\" from input JSON",
        new Object[]{OGIT__ID, m.get(OGIT__ID)});
      m.remove(OGIT__ID);
    }
    if (m.containsKey(OGIT__TYPE)) {
      if (VERTEX_TYPE.equals(m.get(OGIT__TYPE))) {
        m.remove(OGIT__TYPE);
      } else {
        throw new IllegalArgumentException("JSON input defines " + OGIT__TYPE + "="
          + m.get(OGIT__TYPE) + " but only " + VERTEX_TYPE + " is allowed here");
      }
    }
    if (!m.containsKey(OGIT_STATUS)) {
      m.put(OGIT_STATUS, ISSUE_NEW);
    }
    return m;
  }
}

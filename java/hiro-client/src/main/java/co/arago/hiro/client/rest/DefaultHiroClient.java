package co.arago.hiro.client.rest;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.api.LogValue;
import co.arago.hiro.client.api.TimeseriesValue;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.util.DefaultLogValue;
import co.arago.hiro.client.util.DefaultTimeseriesValue;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import co.arago.hiro.client.util.Listener;
import co.arago.hiro.client.util.SimpleWsListener;
import co.arago.hiro.client.util.WsUpgradeLogger;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONValue;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.client.io.UpgradeListener;

import static co.arago.hiro.client.api.RestClient.*;
import static co.arago.hiro.client.util.Helper.*;

public class DefaultHiroClient implements HiroClient {
  
  private static final Logger LOG = Logger.getLogger(DefaultHiroClient.class.getName());
  // default API: "/", versioned API: /api/<version>/graph/
  public static final String DEFAULT_API_VERSION = "7.0";
  public static final String API_PREFIX = "api";
  public static final String API_HELP_PREFIX = "help";
  public static final String API_SUFFIX = "graph";
  public static final String VAR_API_VERSION = "6";
  public static final String VAR_API_SUFFIX = URL_PATH_VARIABLES;
  public static final String EVENT_STREAM_VERSION = "6.1";
  public static final String EVENT_STREAM_SUFFIX = "events-ws";
  private final AuthenticatedRestClient restClient;
  private final AuthenticatedRestClient varClient;
  private final TokenProvider tokenProvider;
  private final String restApiUrl;
  // only for getEventStream:
  private final boolean trustAllCerts;
  private final Level debugLevel;
  
  public DefaultHiroClient(String restApiUrl, TokenProvider tokenProvider, boolean trustAllCerts, Level debugLevel) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, 0, "");  // timeout=0 means no setting/default
  }
  
  public DefaultHiroClient(String restApiUrl, TokenProvider tokenProvider, boolean trustAllCerts, Level debugLevel, int timeout) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, timeout, "");
  }

  // if client is set then trustAllCerts and timeout are already set
  public DefaultHiroClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client, Level debugLevel) {
    this(restApiUrl, tokenProvider, null, false, debugLevel, 0, "");  // timeout=0 means no setting/default
  }

  // still needed for ClientBuilder => public
  public DefaultHiroClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client, boolean trustAllCerts, Level debugLevel, int timeout, String apiVersion) {
    String apiPath;
    if (apiVersion != null && !apiVersion.isEmpty()) {
      apiPath = StringUtils.join(HiroCollections.newList(API_PREFIX, apiVersion, API_SUFFIX), "/");
    } else if (apiVersion != null && apiVersion.isEmpty()) {
      apiPath = "";//6.0 graph
    } else {
      apiPath = StringUtils.join(HiroCollections.newList(API_PREFIX, DEFAULT_API_VERSION, API_SUFFIX), "/");
    }
    
    this.restClient = new AuthenticatedRestClient(restApiUrl, tokenProvider, client, trustAllCerts, debugLevel, timeout, apiPath);
    this.varClient = new AuthenticatedRestClient(restApiUrl, tokenProvider, client, trustAllCerts, debugLevel, timeout, StringUtils.join(HiroCollections.newList(API_PREFIX, VAR_API_VERSION), "/"));
    this.tokenProvider = tokenProvider;
    this.restApiUrl = restApiUrl;
    // used for event stream:
    this.trustAllCerts = trustAllCerts;
    this.debugLevel = debugLevel != null ? debugLevel : Level.OFF;
    LOG.setLevel(this.debugLevel);
  }
  
  @Override
  public void close() throws IOException {
    restClient.close();
    varClient.close();
  }
  
  @Override
  public Map getVertex(String vertexId, Map<String, String> reqParams) {
    String result = restClient.get(HiroCollections.newList(notEmpty(vertexId, "vertexId")), reqParams);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map deleteVertex(String vertexId, Map<String, String> reqParams) {
    String result = restClient.delete(HiroCollections.newList(notEmpty(vertexId, "vertexId")), reqParams);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map createVertex(String vertexType, Map v, Map<String, String> reqParams) {
    String result = restClient.post(HiroCollections.newList(URL_PATH_CREATE, notEmpty(vertexType, "vertexType")),
      Helper.composeJson(v), reqParams);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map updateVertex(String vertexId, Map v, Map<String, String> reqParams) {
    String result = restClient.post(HiroCollections.newList(notEmpty(vertexId, "vertexId")),
      Helper.composeJson(v), reqParams);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map connect(String fromVertexId, String edgeType, String toVertexId) {
    Map<String, Object> m = HiroCollections.newMap();
    m.put(JSON_EDGE_OUT, notEmpty(fromVertexId, "fromVertexId"));
    m.put(JSON_EDGE_IN, notEmpty(toVertexId, "toVertexId"));
    String result = restClient.post(HiroCollections.newList(URL_PATH_CONNECT, notEmpty(edgeType, "edgeType")), Helper.composeJson(m));
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map disconnect(String fromVertexId, String edgeType, String toVertexId) {
    String result = restClient.delete(HiroCollections.newList(Helper.composeEdgeId(notEmpty(fromVertexId, "fromVertexId"), notEmpty(edgeType, "edgeType"), notEmpty(toVertexId, "toVertexId"))), HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public List<Map> getVertexHistory(String vertexId, Map<String, String> queryParams) {
    String result = restClient.get(HiroCollections.newList(notEmpty(vertexId, "vertexId"), URL_PATH_HISTORY), notNull(queryParams, "queryParams"));
    return Helper.parseItemListOfMaps(result);
  }
  
  @Override
  public void historyEvents(Map<String, String> queryParams, Listener<Map> listener) {
    restClient.get(HiroCollections.newList(URL_PATH_EVENTS), notNull(queryParams, "queryParams"), listener);
  }
  
  @Override
  public Map me(Map<String, String> queryParams) {
    String result = restClient.get(HiroCollections.newList(URL_PATH_ME), queryParams);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map me() {
    return me(HiroCollections.newMap());
  }
  
  @Override
  public Map info() {
    Map<String, String> params = HiroCollections.newMap();
    String result = restClient.get(HiroCollections.newList(URL_PATH_INFO), params);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map apiVersion() {
    Map<String, String> params = HiroCollections.newMap();
    String result = restClient.get(StringUtils.join(HiroCollections.newList(API_HELP_PREFIX, URL_PATH_VERSION), "/"), params);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public List<Map> vertexQuery(String query, Map<String, String> queryParams) {
    notNull(queryParams, "queryParams").put(PARAM_QUERY, notEmpty(query, "query"));
    String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_VERTICES), Helper.composeJson(queryParams), null);
    return Helper.parseItemListOfMaps(result);
  }
  
  @Override
  public List<Object> vertexQueryObject(String query, Map<String, String> queryParams) {
    notNull(queryParams, "queryParams").put(PARAM_QUERY, notEmpty(query, "query"));
    String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_VERTICES), Helper.composeJson(queryParams), null);
    return parseResponseObject(result);
  }
  
  @Override
  public List<Map> graphQuery(String rootVertex, String query, Map<String, String> queryParams) {
    notNull(queryParams, "queryParams").put(PARAM_QUERY, notEmpty(query, "query"));
    queryParams.put(PARAM_ROOT, notEmpty(rootVertex, "rootVertex"));
    String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_GREMLIN), Helper.composeJson(queryParams), null);
    return Helper.parseItemListOfMaps(result);
  }
  
  @Override
  public List<Object> graphQueryObject(String rootVertex, String query, Map<String, String> queryParams) {
    notNull(queryParams, "queryParams").put(PARAM_QUERY, notEmpty(query, "query"));
    queryParams.put(PARAM_ROOT, notEmpty(rootVertex, "rootVertex"));
    String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_GREMLIN), Helper.composeJson(queryParams), null);
    return parseResponseObject(result);
  }
  
  @Override
  public List<Map> idQuery(List<String> vertexIds, Map<String, String> queryParams) {
    final Map map = HiroCollections.newMap(PARAM_QUERY, StringUtils.join(notEmpty(vertexIds, "vertexIds"), ","));
    String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_IDS),
      Helper.composeJson(map), queryParams);
    return Helper.parseItemListOfMaps(result);
  }
  
  @Override
  public List<Map> xidQuery(String externalId, Map<String, String> queryParams) {
    String result = restClient.get(HiroCollections.newList(URL_PATH_XID, notEmpty(externalId, "externalId")), notNull(queryParams, "queryParams"));
    return Helper.parseItemListOfMaps(result);
  }
  
  @Override
  public void updateTsValues(String tsNodeId, List<TimeseriesValue> values) {
    restClient.post(HiroCollections.newList(notEmpty(tsNodeId, "tsNodeId"), URL_PATH_VALUES), Helper.composeJson(notEmpty(values, "values")));
  }
  
  @Override
  public void updateTsValuesSynchronous(String tsNodeId, List<TimeseriesValue> values) {
    restClient.post(HiroCollections.newList(notEmpty(tsNodeId, "tsNodeId"), URL_PATH_VALUES), Helper.composeJson(notEmpty(values, "values")), HiroCollections.newMap(QUERY_PARAM_TS_SYNC, "true"));
  }
  
  @Override
  public List<TimeseriesValue> getTsValues(String tsNodeId, long from, long to) {
    return getTsValues(tsNodeId, from, to, HiroCollections.newMap());
  }
  
  @Override
  public List<TimeseriesValue> getTsValues(String tsNodeId, long from, long to, String combinedWith) {
    Map<String, String> queryParams = HiroCollections.newMap();
    if (combinedWith != null && !combinedWith.isEmpty()) {
      queryParams.put(QUERY_PARAM_WITH, combinedWith);
    }
    
    return getTsValues(tsNodeId, from, to, queryParams);
  }
  
  @Override
  public List<TimeseriesValue> getTsValues(String tsNodeId, long from, long to, Map<String, String> queryParams) {
    List<TimeseriesValue> tsvList = HiroCollections.newList();
    Map<String, String> copyParams = HiroCollections.newMap(queryParams);
    copyParams.put(QUERY_PARAM_FROM, Long.toString(from));
    copyParams.put(QUERY_PARAM_TO, Long.toString(to));
    
    Map tmpResult = Helper.parseJsonBody(restClient.get(HiroCollections.newList(notEmpty(tsNodeId, "tsNodeId"), URL_PATH_VALUES), copyParams));
    // TODO lots of error handling missing:
    if (tmpResult.containsKey(JSON_LIST_INDICATOR)) {
      for (Object o : (List) tmpResult.get(JSON_LIST_INDICATOR)) {
        Map entry = (Map) o;
        tsvList.add(new DefaultTimeseriesValue((long) entry.get(JSON_TS_TIMESTAMP), (String) entry.get(JSON_TS_VALUE)));
      }
    } else {
      throw new RuntimeException("Got unexpected response: " + tmpResult.toString());
    }
    return tsvList;
  }

  @Override
  public Map<Long, String> getTsValueHistory(String tsNodeId, long timestamp) {
    Map<String, String> params = HiroCollections.newMap();
    params.put(QUERY_PARAM_TIMESTAMP, Long.toString(timestamp));
    return Helper.parseJsonBody(restClient.get(HiroCollections.newList(notEmpty(tsNodeId, "tsNodeId"), URL_PATH_VALUES, URL_PATH_HISTORY), params));
  }
  
  @Override
  public String updateContent(String attachmentNodeId, InputStream is) {
    return (String) Helper.parseJsonBody(restClient.postBinary(HiroCollections.newList(notEmpty(attachmentNodeId, "attachmentNodeId"), URL_PATH_ATTACHMENT), is)).get("contentId");
  }
  
  @Override
  public InputStream getContent(String attachmentNodeId) {
    return restClient.getBinary(HiroCollections.newList(notEmpty(attachmentNodeId, "attachmentNodeId"),
      URL_PATH_ATTACHMENT), HiroCollections.newMap());
  }
  
  @Override
  public InputStream getContent(String attachmentNodeId, Map<String, String> queryParams) {
    return restClient.getBinary(HiroCollections.newList(notEmpty(attachmentNodeId, "attachmentNodeId"),
      URL_PATH_ATTACHMENT), queryParams);
  }
  
  @Override
  public Map setVariable(String name, String description, boolean isTodoVariable) {
    Map<String, Object> map = HiroCollections.newMap();
    map.put("ogit/name", notEmpty(name, "name"));
    map.put("ogit/description", notEmpty(description, "description"));
    if (isTodoVariable) {
      map.put("ogit/Automation/todo", Boolean.TRUE);
    } else {
      map.put("ogit/Automation/todo", Boolean.FALSE);
    }
    
    return setVariable(map);
  }
  
  @Override
  public Map setVariable(Map kwargs) {
    String result = varClient.put(HiroCollections.newList(VAR_API_SUFFIX), Helper.composeJson(notNull(kwargs, "kwargs")), null);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map getVariable(String name) {
    Map<String, String> parameters = HiroCollections.newMap();
    parameters.put(QUERY_PARAM_NAME, notEmpty(name, "name"));
    String result = varClient.get(HiroCollections.newList(VAR_API_SUFFIX, URL_SUBPATH_VAR_DEFINE), parameters);
    return Helper.parseJsonBody(result);
  }
  
  private List<Object> parseResponseObject(String json) {
    Map body = Helper.parseJsonBody(json);
    if (body.containsKey(JSON_LIST_INDICATOR)) {
      return (List<Object>) body.get(JSON_LIST_INDICATOR);
    } else {
      throw new RuntimeException("did not find list indicator in JSON response: " + json);
    }
  }
  
  @Override
  public void getEventStream(Map<String, String> requestParameters, Listener<String> dataListener, Listener<String> logListener) {
    Long listeningTime = Long.parseLong(notEmpty(requestParameters.get("timeout"), "timeout"));
    final WebSocketClient webSocketClient = new WebSocketClient();
    webSocketClient.getHttpClient().getSslContextFactory().setTrustAll(trustAllCerts);
    try {
      webSocketClient.start();
      URI uri = new URI(this.restApiUrl.replace("http", "ws") + "/"
        + StringUtils.join(HiroCollections.newList(API_PREFIX, EVENT_STREAM_VERSION, EVENT_STREAM_SUFFIX), "/")
        + prepareEventStreamParams(requestParameters));
      final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
        StringBuilder sb = new StringBuilder();
        sb.append("WS-CONNECT [\n  Uri=");
        sb.append(uri.toString());
        for (Entry<String, String> el : requestParameters.entrySet()) {
          sb.append("\n  ");
          sb.append(el.getKey());
          sb.append("=");
          sb.append(el.getValue());
        }
        sb.append("\n]");
        LOG.info(sb.toString());
      clientUpgradeRequest.setRequestURI(uri);
      clientUpgradeRequest.setSubProtocols(getSubProtocols());
      UpgradeListener upgrListener = new WsUpgradeLogger(debugLevel);
      try (Session session = webSocketClient.connect(new SimpleWsListener(requestParameters.get("filter"), dataListener, logListener), uri, clientUpgradeRequest, upgrListener).get()) {
          sb = new StringBuilder();
          sb.append("WS-CONNECT [\n  Remote=");
          sb.append(session.getRemoteAddress().getHostName());
          sb.append("\n  Protocol=");
          sb.append(session.getProtocolVersion());
          sb.append("\n  IdleTimeout=");
          sb.append(session.getIdleTimeout());
          sb.append("\n]");
          LOG.info(sb.toString());
        Thread.sleep(listeningTime);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        webSocketClient.stop();
      } catch (Exception ex) {
        //ignored
      }
      dataListener.onFinish();
      logListener.onFinish();
    }
  }
  
  protected List getSubProtocols() {
    final List protocols = new ArrayList();
    protocols.add("events-1.0.0");
    protocols.add("token-" + tokenProvider.getToken());
    return protocols;
  }
  
  private String prepareEventStreamParams(Map<String, String> params) {
    String ret = "?";
    if (params.containsKey("groupId")) {
      ret += "&groupId=" + params.get("groupId");
    }
    if (params.containsKey("consumerId")) {
      ret += "&consumerId=" + params.get("consumerId");
    }
    if (params.containsKey("offset")) {
      ret += "&offset=" + params.get("offsets");
    }
    if (params.containsKey("delta")) {
      ret += "&delta=" + params.get("delta");
    }
    ret = ret.replace("?&", "?");
    return ret;
  }
  
  @Override
  public void updateLogValues(String logNodeId, List<LogValue> values) {
    restClient.post(HiroCollections.newList(notEmpty(logNodeId, "logNodeId"), URL_PATH_LOGS), JSONValue.toJSONString(notEmpty(values, "values")));
  }
  
  @Override
  public List<LogValue> getLogValues(String logNodeId, long from, long to) {
    List<LogValue> tsvList = HiroCollections.newList();
    Map<String, String> queryParams = HiroCollections.newMap();
    queryParams.put(QUERY_PARAM_FROM, Long.toString(from));
    queryParams.put(QUERY_PARAM_TO, Long.toString(to));
    
    Map tmpResult = Helper.parseJsonBody(restClient.get(HiroCollections.newList(notEmpty(logNodeId, "logNodeId"), URL_PATH_LOGS), queryParams));
    // TODO lots of error handling missing:
    if (tmpResult.containsKey(JSON_LIST_INDICATOR)) {
      for (Object o : (List) tmpResult.get(JSON_LIST_INDICATOR)) {
        Map entry = (Map) o;
        tsvList.add(new DefaultLogValue((String) entry.get(LogValue.JSON_LOG_CONTENT), (long) entry.get(LogValue.JSON_LOG_TIMESTAMP), (String) entry.get(LogValue.JSON_LOG_LEVEL)));
      }
    } else {
      throw new RuntimeException("Got unexpected response: " + tmpResult.toString());
    }
    return tsvList;
  }
  
  @Override
  public void deleteLogValues(String logNodeId, long from, long to) {
    Map<String, String> queryParams = HiroCollections.newMap();
    queryParams.put(QUERY_PARAM_FROM, Long.toString(from));
    queryParams.put(QUERY_PARAM_TO, Long.toString(to));
    restClient.delete(HiroCollections.newList(notEmpty(logNodeId, "logNodeId"), URL_PATH_LOGS), queryParams);
  }
  
  @Override
  public Map getMeProfile() {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ME);
    paths.add(URL_PATH_PROFILE);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map updateMeProfile(Map<String, String> attributes) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ME);
    paths.add(URL_PATH_PROFILE);
    final String result = restClient.post(paths, Helper.composeJson(attributes), attributes);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public InputStream getMeAvatar() {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ME);
    paths.add(URL_PATH_AVATAR);
    return restClient.getBinaryFromStaticLocation(restClient.getRedirectLocation(paths, HiroCollections.newMap()));
  }
  
  @Override
  public Map meAccount(Map<String, String> requestParameters) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ME);
    paths.add(URL_PATH_ACCOUNT);
    final String result = restClient.get(paths, requestParameters);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public Map mePassword(String oldPassword, String newPassword) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ME);
    paths.add(URL_PATH_PASSWORD);
    final Map params = HiroCollections.newMap();
    params.put("oldPassword", oldPassword);
    params.put("newPassword", newPassword);
    final String result = restClient.put(paths, Helper.composeJson(params), null);
    return Helper.parseJsonBody(result);
  }
  
  @Override
  public List<Map> meTeams(boolean includeVirtualTeams) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ME);
    paths.add(URL_PATH_TEAMS);
    final Map params = HiroCollections.newMap();
    if (includeVirtualTeams) {
      params.put(QUERY_PARAM_VIRTUAL_TEAMS, "true");
    }
    final String result = restClient.get(paths, params);
    return Helper.parseItemListOfMaps(result);
  }
  
  @Override
  public List<String> meRoles() {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ME);
    paths.add(URL_PATH_ROLES);
    final Map params = HiroCollections.newMap();
    final String result = restClient.get(paths, params);
    return Helper.parseItemListOfStrings(result);
  }
  
  @Override
  public void updateMeAvatar(InputStream is, String contentType) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ME);
    paths.add(URL_PATH_AVATAR);
    restClient.putBinary(paths, is, HiroCollections.newMap(HEADER_CONTENT_TYPE, contentType));
  }
}

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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.minidev.json.JSONValue;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import static co.arago.hiro.client.util.Helper.*;

public class DefaultHiroClient implements HiroClient {

    private final AuthenticatedRestClient restClient;
    private final TokenProvider tokenProvider;
    private final String restApiUrl;

    public DefaultHiroClient(String restApiUrl, TokenProvider tokenProvider, boolean trustAllCerts, Level debugLevel) {
        this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, 0); // timeout=0 means no setting/default
    }

    public DefaultHiroClient(String restApiUrl, TokenProvider tokenProvider, boolean trustAllCerts, Level debugLevel,
            int timeout) {
        this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, timeout);
    }

    // if client is set then trustAllCerts and timeout are already set
    public DefaultHiroClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client, Level debugLevel) {
        this(restApiUrl, tokenProvider, null, false, debugLevel, 0); // timeout=0 means no setting/default
    }

    // still needed for ClientBuilder => public
    public DefaultHiroClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client,
            boolean trustAllCerts, Level debugLevel, int timeout) {
        restClient = new AuthenticatedRestClient(restApiUrl, tokenProvider, client, trustAllCerts, debugLevel, timeout);
        this.tokenProvider = tokenProvider;
        this.restApiUrl = restApiUrl;
    }

    @Override
    public void close() throws IOException {
        restClient.close();
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
        String result = restClient.post(HiroCollections.newList(notEmpty(vertexId, "vertexId")), Helper.composeJson(v),
                reqParams);
        return Helper.parseJsonBody(result);
    }

    @Override
    public Map connect(String fromVertexId, String edgeType, String toVertexId) {
        Map<String, Object> m = HiroCollections.newMap();
        m.put(JSON_EDGE_OUT, notEmpty(fromVertexId, "fromVertexId"));
        m.put(JSON_EDGE_IN, notEmpty(toVertexId, "toVertexId"));
        String result = restClient.post(HiroCollections.newList(URL_PATH_CONNECT, notEmpty(edgeType, "edgeType")),
                Helper.composeJson(m));
        return Helper.parseJsonBody(result);
    }

    @Override
    public Map disconnect(String fromVertexId, String edgeType, String toVertexId) {
        String result = restClient
                .delete(HiroCollections.newList(Helper.composeEdgeId(notEmpty(fromVertexId, "fromVertexId"),
                        notEmpty(edgeType, "edgeType"), notEmpty(toVertexId, "toVertexId"))), HiroCollections.newMap());
        return Helper.parseJsonBody(result);
    }

    @Override
    public List<Map> getVertexHistory(String vertexId, Map<String, String> queryParams) {
        String result = restClient.get(HiroCollections.newList(notEmpty(vertexId, "vertexId"), URL_PATH_HISTORY),
                notNull(queryParams, "queryParams"));
        return Helper.parseItemList(result);
    }

    @Override
    public void historyEvents(Map<String, String> queryParams, Listener<Map> listener) {
        restClient.get(HiroCollections.newList(URL_PATH_EVENTS), notNull(queryParams, "queryParams"), listener);
    }

    @Override
    public List<Map> getVertexNeighbours(String vertexId, String edgeType, Direction dir) {
        Map params = HiroCollections.newMap();
        params.put(QUERY_PARAM_DIRECTION, notNull(dir, "dir").toString());
        String result = restClient
                .get(HiroCollections.newList(notEmpty(vertexId, "vertexId"), notEmpty(edgeType, "edgeType")), params);
        return Helper.parseItemList(result);
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
    public List<Map> vertexQuery(String query, Map<String, String> queryParams) {
        notNull(queryParams, "queryParams").put(PARAM_QUERY, notEmpty(query, "query"));
        String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_VERTICES),
                Helper.composeJson(queryParams), null);
        return Helper.parseItemList(result);
    }

    @Override
    public List<Object> vertexQueryObject(String query, Map<String, String> queryParams) {
        notNull(queryParams, "queryParams").put(PARAM_QUERY, notEmpty(query, "query"));
        String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_VERTICES),
                Helper.composeJson(queryParams), null);
        return parseResponseObject(result);
    }

    @Override
    public List<Map> graphQuery(String rootVertex, String query, Map<String, String> queryParams) {
        notNull(queryParams, "queryParams").put(PARAM_QUERY, notEmpty(query, "query"));
        queryParams.put(PARAM_ROOT, notEmpty(rootVertex, "rootVertex"));
        String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_GREMLIN),
                Helper.composeJson(queryParams), null);
        return Helper.parseItemList(result);
    }

    @Override
    public List<Object> graphQueryObject(String rootVertex, String query, Map<String, String> queryParams) {
        notNull(queryParams, "queryParams").put(PARAM_QUERY, notEmpty(query, "query"));
        queryParams.put(PARAM_ROOT, notEmpty(rootVertex, "rootVertex"));
        String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_GREMLIN),
                Helper.composeJson(queryParams), null);
        return parseResponseObject(result);
    }

    @Override
    public List<Map> idQuery(List<String> vertexIds, Map<String, String> queryParams) {
        final Map map = HiroCollections.newMap(PARAM_QUERY, StringUtils.join(notEmpty(vertexIds, "vertexIds"), ","));
        String result = restClient.post(HiroCollections.newList(URL_PATH_QUERY, QUERY_TYPE_IDS),
                Helper.composeJson(map), queryParams);
        return Helper.parseItemList(result);
    }

    @Override
    public List<Map> xidQuery(String externalId, Map<String, String> queryParams) {
        String result = restClient.get(HiroCollections.newList(URL_PATH_XID, notEmpty(externalId, "externalId")),
                notNull(queryParams, "queryParams"));
        return Helper.parseItemList(result);
    }

    @Override
    public void updateTsValues(String tsNodeId, List<TimeseriesValue> values) {
        restClient.post(HiroCollections.newList(notEmpty(tsNodeId, "tsNodeId"), URL_PATH_VALUES),
                Helper.composeJson(notEmpty(values, "values")));
    }

    @Override
    public List<TimeseriesValue> getTsValues(String tsNodeId, long from, long to) {
        return getTsValues(tsNodeId, from, to, HiroCollections.newMap());
    }

    @Override
    public List<TimeseriesValue> getTsValues(String tsNodeId, long from, long to, String combinedWith) {
        List<TimeseriesValue> tsvList = HiroCollections.newList();
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
        queryParams.put(QUERY_PARAM_FROM, Long.toString(from));
        queryParams.put(QUERY_PARAM_TO, Long.toString(to));

        Map tmpResult = Helper.parseJsonBody(
                restClient.get(HiroCollections.newList(notEmpty(tsNodeId, "tsNodeId"), URL_PATH_VALUES), queryParams));
        // TODO lots of error handling missing:
        if (tmpResult.containsKey(JSON_LIST_INDICATOR)) {
            for (Object o : (List) tmpResult.get(JSON_LIST_INDICATOR)) {
                Map entry = (Map) o;
                tsvList.add(new DefaultTimeseriesValue((long) entry.get(JSON_TS_TIMESTAMP),
                        (String) entry.get(JSON_TS_VALUE)));
            }
        } else {
            throw new RuntimeException("Got unexpected response: " + tmpResult.toString());
        }
        return tsvList;
    }

    @Override
    public void updateContent(String attachmentNodeId, InputStream is) {
        restClient.postBinary(
                HiroCollections.newList(notEmpty(attachmentNodeId, "attachmentNodeId"), URL_PATH_ATTACHMENT), is);
    }

    @Override
    public InputStream getContent(String attachmentNodeId) {
        return restClient.getBinary(
                HiroCollections.newList(notEmpty(attachmentNodeId, "attachmentNodeId"), URL_PATH_ATTACHMENT),
                HiroCollections.newMap());
    }

    @Override
    public InputStream getContent(String attachmentNodeId, Map<String, String> queryParams) {
        return restClient.getBinary(
                HiroCollections.newList(notEmpty(attachmentNodeId, "attachmentNodeId"), URL_PATH_ATTACHMENT),
                queryParams);
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
        String result = restClient.put(HiroCollections.newList(URL_PATH_VARIABLES),
                Helper.composeJson(notNull(kwargs, "kwargs")), null);
        return Helper.parseJsonBody(result);
    }

    @Override
    public Map getVariable(String name) {
        Map<String, String> parameters = HiroCollections.newMap();
        parameters.put(QUERY_PARAM_NAME, notEmpty(name, "name"));
        String result = restClient.get(HiroCollections.newList(URL_PATH_VARIABLES, URL_SUBPATH_VAR_DEFINE), parameters);
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
    public void getEventStream(Map<String, String> requestParameters, Listener<String> dataListener,
            Listener<String> logListener) {
        final WebSocketClient webSocketClient = new WebSocketClient();
        try {
            webSocketClient.start();
            URI uri = new URI(
                    this.restApiUrl.replace("http", "ws") + "/_events?" + prepareEventStreamPArams(requestParameters));
            final ClientUpgradeRequest clientUpgradeRequest = new ClientUpgradeRequest();
            clientUpgradeRequest.setSubProtocols(getSubProtocols());
            try (Session session = webSocketClient
                    .connect(new SimpleWsListener(requestParameters.get("filter"), dataListener, logListener), uri,
                            clientUpgradeRequest)
                    .get()) {
                Thread.sleep(Long.parseLong(requestParameters.get("timeout")));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                webSocketClient.stop();
            } catch (Exception ex) {
                // ignored
            }
            dataListener.onFinish();
            logListener.onFinish();
        }
    }

    private List getSubProtocols() {
        final List protocols = new ArrayList();
        protocols.add("events-1.0.0");
        protocols.add("token-" + tokenProvider.getToken());
        return protocols;
    }

    private String prepareEventStreamPArams(Map<String, String> params) {
        String ret = "";
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
        return ret;
    }

    @Override
    public void updateLogValues(String logNodeId, List<LogValue> values) {
        restClient.post(HiroCollections.newList(notEmpty(logNodeId, "logNodeId"), URL_PATH_LOGS),
                JSONValue.toJSONString(notEmpty(values, "values")));
    }

    @Override
    public List<LogValue> getLogValues(String logNodeId, long from, long to) {
        List<LogValue> tsvList = HiroCollections.newList();
        Map<String, String> queryParams = HiroCollections.newMap();
        queryParams.put(QUERY_PARAM_FROM, Long.toString(from));
        queryParams.put(QUERY_PARAM_TO, Long.toString(to));

        Map tmpResult = Helper.parseJsonBody(
                restClient.get(HiroCollections.newList(notEmpty(logNodeId, "logNodeId"), URL_PATH_LOGS), queryParams));
        // TODO lots of error handling missing:
        if (tmpResult.containsKey(JSON_LIST_INDICATOR)) {
            for (Object o : (List) tmpResult.get(JSON_LIST_INDICATOR)) {
                Map entry = (Map) o;
                tsvList.add(new DefaultLogValue((String) entry.get(LogValue.JSON_LOG_CONTENT),
                        (long) entry.get(LogValue.JSON_LOG_TIMESTAMP), (String) entry.get(LogValue.JSON_LOG_LEVEL)));
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
}

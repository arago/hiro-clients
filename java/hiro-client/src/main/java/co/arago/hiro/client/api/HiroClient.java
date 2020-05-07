package co.arago.hiro.client.api;

import co.arago.hiro.client.util.Listener;
import java.io.Closeable;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fotto
 */
public interface HiroClient extends Closeable {

    enum Direction {
        in, out, both;

        @Override
        public String toString() {
            switch (this) {
            case in:
                return "IN";
            case out:
                return "OUT";
            case both:
                return "BOTH";
            default:
                return "BOTH";
            }
        }
    }

    String EDGE_ID_SEPARATOR = "$$";
    String JSON_ERROR_INDICATOR = "error";
    String JSON_LIST_INDICATOR = "items";
    String JSON_TS_VALUE = "value";
    String JSON_TS_TIMESTAMP = "timestamp";
    String JSON_EDGE_OUT = "out";
    String JSON_EDGE_IN = "in";

    String URL_PATH_CREATE = "new";
    String URL_PATH_VALUES = "values";
    String URL_PATH_CONNECT = "connect";
    String URL_PATH_HISTORY = "history";
    String URL_PATH_EVENTS = "events";
    String URL_PATH_ATTACHMENT = "content";
    String URL_PATH_VARIABLES = "variables";
    String URL_SUBPATH_VAR_DEFINE = "define";
    String URL_PATH_QUERY = "query";
    String URL_PATH_ME = "me";
    String URL_PATH_VERSION = "version";
    String URL_PATH_XID = "xid";
    String URL_PATH_LOGS = "logs";
    String URL_PATH_ACCOUNT = "account";
    String URL_PATH_PROFILE = "profile";
    String URL_PATH_AVATAR = "avatar";
    String URL_PATH_PASSWORD = "password";
    String URL_PATH_TEAMS = "teams";
    String URL_PATH_ROLES = "roles";

    String PARAM_QUERY = "query";
    String PARAM_ROOT = "root";
    String QUERY_TYPE_IDS = "ids";
    String QUERY_TYPE_VERTICES = "vertices";
    String QUERY_TYPE_GREMLIN = "gremlin";

    String QUERY_PARAM_FROM = "from";
    String QUERY_PARAM_TO = "to";
    String QUERY_PARAM_TIMESTAMP = "timestamp";
    String QUERY_PARAM_WITH = "with";
    String QUERY_PARAM_LIMIT = "limit";
    String QUERY_PARAM_OFFSET = "offset";
    String QUERY_PARAM_NAME = "name";
    String QUERY_PARAM_DIRECTION = "direction";
    String QUERY_PARAM_TS_SYNC = "synchronous";
    String QUERY_PARAM_TS_TTL = "ttl";

    String QUERY_PARAM_VIRTUAL_TEAMS = "include-virtual";

    // vertex CRUD
    Map getVertex(String vertexId, Map<String, String> requestParameters);

    Map deleteVertex(String vertexId, Map<String, String> requestParameters);

    Map createVertex(String vertexType, Map vertexAttributes, Map<String, String> requestParameters);

    Map updateVertex(String vertexId, Map vertexAttributes, Map<String, String> requestParameters);

    // edge handling
    Map connect(String fromVertexId, String edgeType, String toVertexId);

    Map disconnect(String fromVertexId, String edgeType, String toVertexId);

    // special vertex operations
    List<Map> getVertexHistory(String vertexId, Map<String, String> requestParameters);

    // queries
    List<Map> vertexQuery(String query, Map<String, String> requestParameters);

    List<Object> vertexQueryObject(String query, Map<String, String> requestParameters);

    List<Map> graphQuery(String rootVertex, String query, Map<String, String> requestParameters);

    List<Object> graphQueryObject(String rootVertex, String query, Map<String, String> requestParameters);

    List<Map> idQuery(List<String> vertexIds, Map<String, String> requestParameters);

    List<Map> xidQuery(String externalId, Map<String, String> requestParameters);

    // get history events of the graph
    void historyEvents(Map<String, String> requestParameters, Listener<Map> listener);

    // timeseries value handling
    void updateTsValues(String tsNodeId, List<TimeseriesValue> values);

    void updateTsValuesSynchronous(String tsNodeId, List<TimeseriesValue> values);

    void updateTsValues(String tsNodeId, List<TimeseriesValue> values, String ttl);

    void updateTsValuesSynchronous(String tsNodeId, List<TimeseriesValue> values, String ttl);

    List<TimeseriesValue> getTsValues(String tsNodeId, long from, long to);

    List<TimeseriesValue> getTsValues(String tsNodeId, long from, long to, Map<String, String> requestParameters);

    List<TimeseriesValue> getTsValues(String tsNodeId, long from, long to, String combinedWith);

    Map<Long, String> getTsValueHistory(String tsNodeId, long timestamp);

    // attachment handling
    String updateContent(String attachmentNodeId, InputStream is);

    InputStream getContent(String attachmentNodeId);

    InputStream getContent(String attachmentNodeId, Map<String, String> requestParameters);

    // variables handling
    Map setVariable(String name, String description, boolean isTodoVariable);

    Map setVariable(Map kwargs);

    Map getVariable(String name);
    // TODO: add variable search methods?

    // special calls
    Map apiVersion();
    Map apiVersions();

    // get ws event stream
    // will be removed without notice
    @Deprecated
    void getEventStream(Map<String, String> requestParameters, Listener<String> msgListener,
            Listener<String> metaListener);

    // Logs
    @Deprecated
    void updateLogValues(String logNodeId, List<LogValue> values);

    @Deprecated
    List<LogValue> getLogValues(String logNodeId, long from, long to);

    @Deprecated
    void deleteLogValues(String logNodeId, long from, long to);

    Map meAccount(Map<String, String> requestParameters);

    Map getMeProfile();

    Map updateMeProfile(Map<String, String> attributes);

    void updateMeAvatar(InputStream is, String contentType);

    InputStream getMeAvatar();

    Map mePassword(String oldPassword, String newPassword);

    List<Map> meTeams(boolean includeVirtualTeams);

    List<String> meRoles();

    default List<Map> meTeams() {
        return meTeams(false);
    }
}

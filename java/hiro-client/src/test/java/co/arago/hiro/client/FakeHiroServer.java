/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.client;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import co.arago.hiro.client.util.HiroException;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONValue;
import org.apache.commons.lang.StringUtils;

import static co.arago.hiro.client.rest.DefaultHiroClient.*;

/**
 *
 * @author fotto
 */
public class FakeHiroServer extends NanoHTTPD {

    final class Result {

        private final IStatus status;
        private final String message;

        public Result(IStatus status, String message) {
            this.message = message;
            this.status = status;
        }

        public IStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    private static final String FAILURE_MSG = "operation failed";
    private static final String ID_ATTR = "ogit/_id";
    private static final String URL_PREFIX = "/"
            + StringUtils.join(HiroCollections.newList(API_PREFIX, API_SUFFIX, DEFAULT_API_VERSION), "/");

    private final ConcurrentHashMap<String, String> vertices = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> edges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> blobs = new ConcurrentHashMap<>();

    private static final Level defaultLevel = Level.INFO;
    private static final Logger LOG = Logger.getLogger(FakeHiroServer.class.getName());

    public FakeHiroServer(int port) throws IOException {
        super(port);

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            Method method = session.getMethod();
            if (method.equals(method.GET)) {
                return handleGet(session);
            }
            if (method.equals(method.POST)) {
                return handlePost(session);
            }
            if (method.equals(method.PUT)) {
                return handlePut(session);
            }
            if (method.equals(method.DELETE)) {
                return handleDelete(session);
            }

            throw new RuntimeException();
        } catch (IOException ex) {
            Logger.getLogger(FakeHiroServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String stripUri(String uri) {
        if (uri.startsWith(URL_PREFIX)) {
            return uri.substring(URL_PREFIX.length());
        } else {
            return uri;
        }
    }

    private Response handleGet(IHTTPSession session) {
        Result result;
        String uri = stripUri(session.getUri());
        LOG.log(defaultLevel, "GET " + uri);
        String[] split = uri.split("/");
        if (split.length > 2) {
            if (split[1].equals(HiroClient.URL_PATH_QUERY)) {
                LOG.log(defaultLevel, "GET /" + HiroClient.URL_PATH_QUERY);
                result = new Result(Status.NOT_IMPLEMENTED, asJsonError("NOT IMPLEMENTED, YET"));
            } else if (split[1].equals(HiroClient.URL_PATH_VARIABLES)) {
                LOG.log(defaultLevel, "GET /" + HiroClient.URL_PATH_VARIABLES);
                result = new Result(Status.NOT_IMPLEMENTED, asJsonError("NOT IMPLEMENTED, YET"));
            } else {
                // assume split[1] is a vertex
                if (split[2].equals(HiroClient.URL_PATH_HISTORY)) {
                    LOG.log(defaultLevel, "GET /" + HiroClient.URL_PATH_HISTORY);
                    result = getVertexHistory(split[1], session.getParms());
                } else if (split[2].equals(HiroClient.URL_PATH_VALUES)) {
                    LOG.log(defaultLevel, "GET /" + HiroClient.URL_PATH_VALUES);
                    result = new Result(Status.NOT_IMPLEMENTED, asJsonError("NOT IMPLEMENTED, YET"));
                } else if (split[2].equals(HiroClient.URL_PATH_ATTACHMENT)) {
                    LOG.log(defaultLevel, "GET /" + HiroClient.URL_PATH_ATTACHMENT);
                    result = new Result(Status.NOT_IMPLEMENTED, asJsonError("NOT IMPLEMENTED, YET"));
                } else if (split[2].equals(HiroClient.URL_PATH_VERSION)) {
                    LOG.log(defaultLevel, "GET /" + HiroClient.URL_PATH_VERSION);
                    result = new Result(Status.OK,
                            "{\"iam\": {\"endpoint\": \"/api/iam/6.1/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"iam.yaml\", \"version\": \"6.1\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"authz\": {\"endpoint\": \"/api/authz/6.1/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"authz.yaml\", \"version\": \"6.1\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"app-admin\": {\"endpoint\": \"/api/app-admin/1.2/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"app-admin.yaml\", \"version\": \"1.2\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"logs\": {\"endpoint\": \"/api/logs/0.9/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"unsupported\", \"specs\": \"\", \"version\": \"0.9\", \"lifecycle\": \"deprecated\", \"protocols\": \"\"}, \"events-ws\": {\"endpoint\": \"/api/events-ws/6.1/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"events-ws.yaml\", \"version\": \"6.1\", \"lifecycle\": \"deprecated\", \"protocols\": \"events-1.0.0\"}, \"graph\": {\"endpoint\": \"/api/graph/7.2/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"graph.yaml\", \"version\": \"7.2\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"app\": {\"endpoint\": \"/api/app/7.0/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"app.yaml\", \"version\": \"7.0\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"action-ws\": {\"endpoint\": \"/api/action-ws/1.0/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"action-ws.yaml\", \"version\": \"1.0\", \"lifecycle\": \"stable\", \"protocols\": \"action-1.0.0\"}, \"auth\": {\"endpoint\": \"/api/auth/6.2/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"auth.yaml\", \"version\": \"6.2\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"ki\": {\"endpoint\": \"/api/ki/6/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"unsupported\", \"specs\": \"ki.yaml\", \"version\": \"6\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"objects\": {\"endpoint\": \"/api/objects/1.0/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"objects.yaml\", \"version\": \"1.0\", \"lifecycle\": \"experimental\", \"protocols\": \"\"}, \"health\": {\"endpoint\": \"/api/health/7.0/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"health.yaml\", \"version\": \"7.0\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"action\": {\"endpoint\": \"/api/action/1.0/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"action.yaml\", \"version\": \"1.0\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"variables\": {\"endpoint\": \"/api/variables/6/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"unsupported\", \"specs\": \"variables.yaml\", \"version\": \"6\", \"lifecycle\": \"stable\", \"protocols\": \"\"}, \"graph-ws\": {\"endpoint\": \"/api/graph-ws/6.1/\", \"docs\": \"https://developer.hiro.arago.co/api\", \"support\": \"supported\", \"specs\": \"graph-ws.yaml\", \"version\": \"6.1\", \"lifecycle\": \"stable\", \"protocols\": \"graph-2.0.0\"}}");
                } else if (split[4].equals(HiroClient.URL_PATH_EVENTS)) {
                    LOG.log(defaultLevel, "GET /" + HiroClient.URL_PATH_EVENTS);
                    result = getEvents(session.getParms());
                } else {
                    // assume vertex
                    LOG.log(defaultLevel, "GET Vertex");
                    result = getVertex(split[1], session.getParms());
                }
            }
        } else {
            if (split[1].equals(HiroClient.URL_PATH_EVENTS)) {
                LOG.log(defaultLevel, "GET /" + HiroClient.URL_PATH_EVENTS);
                result = getEvents(session.getParms());
            } else {
                // assume vertex
                LOG.log(defaultLevel, "GET Vertex");
                result = getVertex(split[1], session.getParms());
            }
        }

        Response r = newFixedLengthResponse(result.getMessage());
        r.setStatus(result.getStatus());
        return r;
    }

    private Response handlePost(IHTTPSession session) throws IOException {
        String uri = stripUri(session.getUri());
        LOG.log(defaultLevel, "POST " + uri);
        String[] split = uri.split("/");
        Result result;
        if (split.length > 2) {
            if (split[1].equals(HiroClient.URL_PATH_CREATE)) {
                LOG.log(defaultLevel, "POST /" + HiroClient.URL_PATH_CREATE);
                String json = readBody(session.getInputStream());
                result = doCreate(split[2], json);
            } else if (split[1].equals(HiroClient.URL_PATH_CONNECT)) {
                LOG.log(defaultLevel, "POST /" + HiroClient.URL_PATH_CONNECT);
                result = doConnect(split[2], readBody(session.getInputStream()));
            } else if (split[1].equals(HiroClient.URL_PATH_QUERY)) {
                LOG.log(defaultLevel, "POST /" + HiroClient.URL_PATH_QUERY);
                result = new Result(Status.NOT_IMPLEMENTED, asJsonError("NOT IMPLEMENTED, YET"));
            } else if (split[2].equals(HiroClient.URL_PATH_VALUES)) {
                LOG.log(defaultLevel, "POST /" + HiroClient.URL_PATH_VALUES);
                result = uploadValues(split[1], readBody(session.getInputStream()));
            } else if (split[2].equals(HiroClient.URL_PATH_ATTACHMENT)) {
                LOG.log(defaultLevel, "POST /" + HiroClient.URL_PATH_ATTACHMENT);
                result = uploadContent(split[1], readBody(session.getInputStream()));
            } else {
                result = new Result(Status.INTERNAL_ERROR, asJsonError("Unknown operation for uri " + uri));
            }
        } else {
            // assume vertex update
            LOG.log(defaultLevel, "POST Vertex");
            result = doUpdate(split[1], readBody(session.getInputStream()));
        }
        Response r = newFixedLengthResponse(result.getMessage());
        r.setStatus(result.getStatus());
        return r;
    }

    private Response handlePut(IHTTPSession session) throws IOException {
        String uri = stripUri(session.getUri());
        LOG.log(defaultLevel, "PUT " + uri);
        String[] split = uri.split("/");
        Result result;

        if (split[1].equals(HiroClient.URL_PATH_VARIABLES)) {
            LOG.log(defaultLevel, "PUT /" + HiroClient.URL_PATH_VARIABLES);
            result = setVariable(readBody(session.getInputStream()));
        } else {
            throw new IllegalArgumentException("unsupported PUT requested");
        }
        Response r = newFixedLengthResponse(result.getMessage());
        r.setStatus(result.getStatus());
        return r;
    }

    private Response handleDelete(IHTTPSession session) {
        String uri = stripUri(session.getUri());
        LOG.log(defaultLevel, "DELETE " + uri);
        String[] split = uri.split("/");
        Result result;

        LOG.log(defaultLevel, "DELETE Vertex or Edge");
        result = deleteVertexOrEdge(split[1]);

        Response r = newFixedLengthResponse(result.getMessage());
        r.setStatus(result.getStatus());
        return r;
    }

    private Result getVertex(String id, Map<String, String> params) {
        if (!vertices.containsKey(id)) {
            return new Result(Status.NOT_FOUND, asJsonError("vertex " + id + " does not exist"));
        }
        return new Result(Status.OK, vertices.get(id));
    }

    private Result getVertexHistory(String id, Map<String, String> params) {
        // in this fake we always have only one entry in the history
        // hence we ignore the params
        if (!vertices.containsKey(id)) {
            return new Result(Status.NOT_FOUND, asJsonError("vertex " + id + " does not exist"));
        }
        return new Result(Status.OK, "{\"" + HiroClient.JSON_LIST_INDICATOR + "\":[" + vertices.get(id) + "]}");
    }

    private Result getEvents(Map<String, String> params) {
        return new Result(Status.OK,
                "{\"items\":[{\"id\":\"cj67kc74g07ihdr74p0c3sn6i\",\"nanotime\":1390026310752627,\"timestamp\":1502437236523,\"body\":{\"ogit\\/_created-on\":1502437236523,\"\\/marsNodeID\":\"{\\\"ids\\\":[\\\"test3:test3:Machine:HIRO_AllInOne\\\"]}\",\"ogit\\/status\":\"FULFILLED\",\"ogit\\/_id\":\"cj67kc74g07ihdr74p0c3sn6i\",\"\\/formular\":\"{\\\"title\\\":\\\"Get Hostname\\\",\\\"attributes\\\":{\\\"Get\\\":\\\"Hostname\\\"},\\\"nodeid\\\":[\\\"test3:test3:Machine:HIRO_AllInOne\\\"]}\",\"ogit\\/_creator\":\"mglusiuk@arago.de\",\"\\/issue-xmlns\":\"https:\\/\\/graphit.co\\/schemas\\/v2\\/IssueSchema\",\"ogit\\/_graphtype\":\"vertex\",\"ogit\\/_owner\":\"mglusiuk@arago.de\",\"ogit\\/_v\":1,\"ogit\\/_is-deleted\":false,\"ogit\\/_modified-by-app\":\"Frontend openautopilot\",\"ogit\\/_creator-app\":\"Frontend openautopilot\",\"ogit\\/_modified-by\":\"mglusiuk@arago.de\",\"ogit\\/_version\":\"2.19.0.114\",\"ogit\\/_type\":\"ogit\\/Task\"},\"identity\":\"mglusiuk@arago.de\",\"type\":\"CREATE\"},{\"id\":\"mglusiuk@arago.de$$ogit\\/_created$$cj67kc74g07ihdr74p0c3sn6i\",\"nanotime\":1390026341190402,\"timestamp\":1502437236558,\"body\":{\"ogit\\/_created-on\":1502437236558,\"ogit\\/_out-id\":\"mglusiuk@arago.de\",\"ogit\\/_in-type\":\"ogit\\/Task\",\"ogit\\/_out-type\":\"ogit\\/Person\",\"ogit\\/_modified-on\":1502437236558,\"ogit\\/_in-id\":\"cj67kc74g07ihdr74p0c3sn6i\",\"ogit\\/_id\":\"mglusiuk@arago.de$$ogit\\/_created$$cj67kc74g07ihdr74p0c3sn6i\",\"ogit\\/_creator\":\"mglusiuk@arago.de\",\"ogit\\/_graphtype\":\"edge\",\"ogit\\/_edge-id\":\"mglusiuk@arago.de$$ogit\\/_created$$cj67kc74g07ihdr74p0c3sn6i\",\"ogit\\/_is-deleted\":false,\"ogit\\/_modified-by\":\"mglusiuk@arago.de\",\"ogit\\/_type\":\"ogit\\/_created\"},\"identity\":\"mglusiuk@arago.de\",\"type\":\"CONNECT\"},{\"id\":\"mglusiuk@arago.de$$ogit\\/_owns$$cj67kc74g07ihdr74p0c3sn6i\",\"nanotime\":1390026344248254,\"timestamp\":1502437236569,\"body\":{\"ogit\\/_created-on\":1502437236569,\"ogit\\/_out-id\":\"mglusiuk@arago.de\",\"ogit\\/_in-type\":\"ogit\\/Task\",\"ogit\\/_out-type\":\"ogit\\/Person\",\"ogit\\/_modified-on\":1502437236569,\"ogit\\/_in-id\":\"cj67kc74g07ihdr74p0c3sn6i\",\"ogit\\/_id\":\"mglusiuk@arago.de$$ogit\\/_owns$$cj67kc74g07ihdr74p0c3sn6i\",\"ogit\\/_creator\":\"mglusiuk@arago.de\",\"ogit\\/_graphtype\":\"edge\",\"ogit\\/_edge-id\":\"mglusiuk@arago.de$$ogit\\/_owns$$cj67kc74g07ihdr74p0c3sn6i\",\"ogit\\/_is-deleted\":false,\"ogit\\/_modified-by\":\"mglusiuk@arago.de\",\"ogit\\/_type\":\"ogit\\/_owns\"},\"identity\":\"mglusiuk@arago.de\",\"type\":\"CONNECT\"}]}");
    }

    private Result doCreate(String type, String json) {
        int id = vertices.size() + 1;
        Map m = Helper.parseJsonBody(json);
        String idStr = Integer.toString(id);
        m.put(ID_ATTR, idStr);
        m.put("ogit/_is-deleted", "false");
        m.put("ogit/_graphtype", "vertex");
        if (vertices.containsKey(idStr)) {
            throw new HiroException("vertex " + id + " already exists", 409);
        } else {
            vertices.put(idStr, Helper.composeJson(m));
        }
        return new Result(Status.OK, vertices.get(idStr));
    }

    private Result doUpdate(String id, String json) {
        if (vertices.containsKey(id)) {
            Map m = Helper.parseJsonBody(vertices.get(id));
            m.putAll(Helper.parseJsonBody(json));
            for (Object key : m.keySet()) {
                if (m.get(key) == null) {
                    m.remove(key);
                }
            }
            m.put("ogit/_is-deleted", "false");
            m.put("ogit/_graphtype", "vertex");
            m.put("ogit/_id", id);
            vertices.put(id, Helper.composeJson(m));
            return new Result(Status.OK, vertices.get(id));
        } else {
            return new Result(Status.NOT_FOUND, asJsonError("vertex " + id + " does not exist"));
        }
    }

    private Result doConnect(String edgeType, String json) {
        Map m = Helper.parseJsonBody(json);
        String outId = (String) m.get("out");
        String inId = (String) m.get("in");
        if (!vertices.containsKey(outId)) {
            return new Result(Status.NOT_FOUND, asJsonError("out vertex " + outId + " does not exist"));
        }
        if (!vertices.containsKey(inId)) {
            return new Result(Status.NOT_FOUND, asJsonError("in vertex " + inId + " does not exist"));
        }
        String edgeId = Helper.composeEdgeId(outId, edgeType, outId);
        edges.put(edgeId, Arrays.asList(outId, inId));
        return new Result(Status.OK, createEdgeJson(edgeId, "false"));
    }

    private Result deleteVertexOrEdge(String id) {
        if (vertices.containsKey(id)) {
            if (blobs.containsKey(id)) {
                blobs.remove(id);
            }
            List<String> toDelete = new ArrayList<>();
            for (String edgeId : edges.keySet()) {
                for (String vId : edges.get(edgeId)) {
                    if (vId.equals(id)) {
                        toDelete.add(edgeId);
                    }
                }
            }
            for (String edgeId : toDelete) {
                edges.remove(edgeId);
            }
            Map m = Helper.parseJsonBody(vertices.get(id));
            m.put("ogit/_is-deleted", "true");
            vertices.remove(id);
            return new Result(Status.OK, Helper.composeJson(m));
        } else if (edges.containsKey(id)) {
            String result = createEdgeJson(id, "true");
            edges.remove(id);
            return new Result(Status.OK, result);
        } else {
            return new Result(Status.NOT_FOUND, asJsonError("item " + id + " does not exist"));
        }
    }

    private Result uploadValues(String id, String json) {
        // in the fake we don't care about vertex type
        return new Result(Status.NOT_IMPLEMENTED, asJsonError("NOT IMPLEMENTED, YET"));
    }

    private Result uploadContent(String id, String content) {
        // in the fake we don't care about vertex type
        return new Result(Status.NOT_IMPLEMENTED, asJsonError("NOT IMPLEMENTED, YET"));
    }

    private Result setVariable(String json) {
        return new Result(Status.NOT_IMPLEMENTED, asJsonError("NOT IMPLEMENTED, YET"));
    }

    private String readBody(InputStream is) throws IOException {
        byte[] buffer = new byte[10000];
        int noBytes = is.read(buffer);
        ByteBuffer buf = ByteBuffer.allocate(noBytes);
        buf.put(buffer, 0, noBytes);
        return new String(buf.array(), "UTF-8");
    }

    private String createEdgeJson(String id, String deleted) {
        String[] split = id.split(HiroClient.EDGE_ID_SEPARATOR);
        // no error checking.
        String outId = split[0];
        String edgeType = split[1];
        String inId = split[2];
        Map m = new HashMap();
        m.put("ogit/_out-id", outId);
        m.put("ogit/_in-id", inId);
        m.put("ogit/_id", id);
        m.put("ogit/_graphtype", "edge");
        m.put("ogit/_is-deleted", deleted);
        m.put("ogit/_type", edgeType);
        m.put("ogit/_edge-id", id);
        return Helper.composeJson(m);
    }

    private String asJsonError(String msg) {
        return JSONValue.toJSONString(HiroCollections.newMap("error", msg));
    }

}

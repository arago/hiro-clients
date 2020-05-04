/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.client.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minidev.json.JSONValue;

import static co.arago.hiro.client.api.HiroClient.*;

/**
 *
 * @author fotto
 */
public class Helper {

    public static String composeEdgeId(final String fromVertexId, final String edgeType, final String toVertexId) {
        StringBuilder sb = new StringBuilder(fromVertexId);
        sb.append(EDGE_ID_SEPARATOR);
        sb.append(edgeType);
        sb.append(EDGE_ID_SEPARATOR);
        sb.append(toVertexId);
        return sb.toString();
    }

    public static String composeJson(final Map m) {
        return JSONValue.toJSONString(notNull(m, "json"));
    }

    public static String composeJson(final List<co.arago.hiro.client.api.TimeseriesValue> tsvList) {
        Map<String, Object> m = new HashMap();

        m.put(JSON_LIST_INDICATOR, tsvList);

        return composeJson(m);
    }

    // stolen from Stefan's code :-)
    public static Map parseJsonBody(final String data) {
        final Object o = JSONValue.parse(data);
        if (o instanceof Map) {
            return (Map) o;
        } else {
            throw new HiroException("response is not valid JSON: " + data, 500);
        }
    }

    public static Map unwrapJsonMap(final String data) {
        final Object o = JSONValue.parse(data);
        if (o instanceof Map) {
            return (Map) o;
        } else {
            throw new HiroException("response is not valid JSON map: " + data, 500);
        }
    }

    public static List unwrapJsonArray(final String data) {
        final Object o = JSONValue.parse(data);
        if (o instanceof List) {
            return (List) o;
        } else {
            throw new HiroException("response is not valid JSON array: " + data, 500);
        }
    }

    public static String notEmpty(final String ref, final String who) {
        if (ref == null || ref.isEmpty()) {
            throw new HiroException(who + " is empty", 400);
        }

        return ref;
    }

    public static <T extends Collection> T notEmpty(final T ref, final String who) {
        if (ref == null || ref.isEmpty()) {
            throw new HiroException(who + " is empty", 400);
        }

        return (T) ref;
    }

    public static <T> T notNull(final T ref, final String who) {
        if (ref == null) {
            throw new HiroException(who + " is missing", 400);
        }

        return ref;
    }

    public static List<Map> parseItemListOfMaps(String json) {
        Map body = Helper.parseJsonBody(json);
        if (body.containsKey(JSON_ERROR_INDICATOR)) {
            throw new HiroException(body.get(JSON_ERROR_INDICATOR).toString(), 500);
        }
        if (body.containsKey(JSON_LIST_INDICATOR)) {
            Object o1 = body.get(JSON_LIST_INDICATOR);
            List items = (List) o1;
            if (items.isEmpty()) {
                return items;
            } else {
                if (items.get(0) instanceof Map) {
                    return (List<Map>) body.get(JSON_LIST_INDICATOR);
                } else {
                    throw new RuntimeException("JSON response is not a Map, try Object: " + json);
                }
            }
        } else {
            throw new RuntimeException("did not find list indicator in JSON response: " + json);
        }
    }

    public static List<String> parseItemListOfStrings(String json) {
        Map body = Helper.parseJsonBody(json);
        if (body.containsKey(JSON_ERROR_INDICATOR)) {
            throw new HiroException(body.get(JSON_ERROR_INDICATOR).toString(), 500);
        }
        if (body.containsKey(JSON_LIST_INDICATOR)) {
            Object o1 = body.get(JSON_LIST_INDICATOR);
            List items = (List) o1;
            if (items.isEmpty()) {
                return items;
            } else {
                if (items.get(0) instanceof String) {
                    return (List<String>) body.get(JSON_LIST_INDICATOR);
                } else {
                    throw new RuntimeException("JSON response is not a String, try Object: " + json);
                }
            }
        } else {
            throw new RuntimeException("did not find list indicator in JSON response: " + json);
        }
    }

    public static List<Map> parseList(String json) {
        final Object o = JSONValue.parse(json);
        if (o instanceof List) {
            List items = (List) o;
            if (items.isEmpty()) {
                return items;
            } else {
                if (items.get(0) instanceof Map) {
                    return (List<Map>) items;
                } else {
                    throw new RuntimeException("JSON response is not a Map, try Object: " + json);
                }
            }
        } else {
            throw new HiroException("response is not valid JSON: " + json, 500);
        }
    }

    private Helper() {
    }

}

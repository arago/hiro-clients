package co.arago.hiro.client.api;

import java.io.Closeable;
import java.util.Map;

public interface WebSocketClient extends Closeable {

    /**
     * @param type
     * @param headers
     * @param body
     * 
     * @return the id of this request
     */
    long sendMessage(String type, Map<String, String> headers, Map body);

    void sendMessage(String message);

    void addEventFilter(Map filter);

    void removeEventFilter(String id);

    void clearEventFilters();

    void subscribeScope(String scopeId);

    void removeScope(String scopeId);
}

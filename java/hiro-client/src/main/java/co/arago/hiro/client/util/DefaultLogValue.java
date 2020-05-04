package co.arago.hiro.client.util;

import co.arago.hiro.client.api.LogValue;
import java.util.Map;
import net.minidev.json.JSONObject;

public class DefaultLogValue implements LogValue {

    private final String message;
    private final long timestamp;
    private final String level;

    public DefaultLogValue(String message, long timestamp, String level) {
        this.message = message;
        this.timestamp = timestamp;
        this.level = level;
    }

    @Override
    public String toJSONString() {
        final Map el = HiroCollections.newMap();
        el.put(JSON_LOG_CONTENT, message);
        el.put(JSON_LOG_LEVEL, level);
        el.put(JSON_LOG_TIMESTAMP, timestamp);
        return JSONObject.toJSONString(el);
    }

}

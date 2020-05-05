package co.arago.hiro.client.test;

import static co.arago.hiro.client.api.HiroClient.JSON_TS_VALUE;
import co.arago.hiro.client.api.TimeseriesValue;
import co.arago.hiro.client.util.HiroCollections;
import java.util.Map;
import net.minidev.json.JSONObject;

public class InvalidTimeseriesValue implements TimeseriesValue {

    private final long timestamp;
    private final String value;

    public InvalidTimeseriesValue(long timestamp, String value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toJSONString() {
        Map el = HiroCollections.newMap();
        el.put(JSON_TS_VALUE, getValue());

        return JSONObject.toJSONString(el);
    }

    @Override
    public String toString() {
        return "DefaultTimeseriesValue{" + "timestamp=" + timestamp + ", value=" + value + '}';
    }
}

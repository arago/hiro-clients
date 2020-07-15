/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.client.util;

import co.arago.hiro.client.api.TimeseriesValue;
import net.minidev.json.JSONObject;

import java.util.Map;

import static co.arago.hiro.client.api.HiroClient.JSON_TS_TIMESTAMP;
import static co.arago.hiro.client.api.HiroClient.JSON_TS_VALUE;

/**
 *
 * @author fotto
 */
public class DefaultTimeseriesValue implements TimeseriesValue {
    private final long timestamp;
    private final String value;

    public DefaultTimeseriesValue(long timestamp, String value) {
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
        el.put(JSON_TS_TIMESTAMP, getTimestamp());

        return JSONObject.toJSONString(el);
    }

    @Override
    public String toString() {
        return "DefaultTimeseriesValue{" + "timestamp=" + timestamp + ", value=" + value + '}';
    }
}

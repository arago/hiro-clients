package co.arago.hiro.client.api;

import net.minidev.json.JSONAware;

/**
 *
 */
public interface TimeseriesValue extends JSONAware{
    long getTimestamp();
    String getValue();
}

package co.arago.hiro.client.api;

import net.minidev.json.JSONAware;

public interface LogValue extends JSONAware {

    String JSON_LOG_CONTENT = "content";
    String JSON_LOG_TIMESTAMP = "timestamp";
    String JSON_LOG_LEVEL = "level";
}

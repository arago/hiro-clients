package co.arago.hiro.client.rest;

import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.api.WebSocketClient;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import co.arago.hiro.client.util.HiroException;
import co.arago.hiro.client.util.Listener;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONValue;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;

public class DefaultWebSocketClient implements WebSocketClient {

  public static final String DEFAULT_API_VERSION = "6.1";
  public static final String API_PREFIX = "api";
  public static final String API_SUFFIX = "graph-ws";
  private static final Logger LOG = Logger.getLogger(DefaultWebSocketClient.class.getName());
  private final WebSocket webSocketClient;
  private final AtomicInteger idCounter;
  private final Level debugLevel;

  public DefaultWebSocketClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client, Level debugLevel, int timeout, Listener<String> dataListener, Listener<String> loglistener) throws InterruptedException, ExecutionException {
    this.idCounter = new AtomicInteger();
    this.debugLevel = debugLevel;
    WebSocketUpgradeHandler.Builder upgradeHandlerBuilder
      = new WebSocketUpgradeHandler.Builder();
    WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder
      .addWebSocketListener(new WebSocketListener() {
        @Override
        public void onOpen(WebSocket websocket) {
          final Map m = HiroCollections.newMap();
          m.put("type", "open");
          m.put("open", websocket.isOpen());
          loglistener.process(JSONValue.toJSONString(m));
        }

        @Override
        public void onClose(WebSocket websocket, int code, String reason) {
          final Map m = HiroCollections.newMap();
          m.put("type", "close");
          m.put("code", code);
          m.put("reason", reason);
          loglistener.process(JSONValue.toJSONString(m));
        }

        @Override
        public void onError(Throwable t) {
          final Map m = HiroCollections.newMap();
          m.put("type", "error");
          m.put("message", t.getMessage());
          loglistener.process(JSONValue.toJSONString(m));
        }

        @Override
        public void onTextFrame(String payload, boolean finalFragment, int rsv) {
          dataListener.process(payload);
      }

      @Override
      public void onPingFrame(byte[] payload) {
        webSocketClient.sendPongFrame(payload);
      }
      }).build();
    webSocketClient = client
      .prepareGet(restApiUrl.replace("http", "ws") + "/" + StringUtils.join(HiroCollections.newList(API_PREFIX, DEFAULT_API_VERSION, API_SUFFIX), "/"))
      .addHeader("Sec-WebSocket-Protocol", "graph-2.0.0, token-" + tokenProvider.getToken())
      .setRequestTimeout(timeout)
      .execute(wsHandler)
      .get();
  }

  @Override
  public int sendMessage(String type, Map<String, String> headers, Map body) {
    if (webSocketClient.isOpen()) {
      final int id = idCounter.incrementAndGet();
      final Map request = HiroCollections.newMap();
      request.put("id", id);
      request.put("type", type);
      request.put("headers", headers);
      request.put("body", body);
      if (LOG.isLoggable(debugLevel)) {
        LOG.log(debugLevel, Helper.composeJson(request));
      }
      webSocketClient.sendTextFrame(Helper.composeJson(request));
      return id;
    } else {
      throw new HiroException("web socket connection not open", 400);
    }
  }

  @Override
  public void close() {
    if (webSocketClient.isOpen()) {
      webSocketClient.sendCloseFrame(200, "OK");
    }
  }
}

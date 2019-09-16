package co.arago.hiro.client.util;

import java.io.IOException;
import java.util.Map;
import net.minidev.json.JSONValue;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class SimpleWsListener implements WebSocketListener {

  private final Listener<String> datalistener;
  private final Listener<String> loglistener;
  private final String filter;

  public SimpleWsListener(String filter, Listener<String> datalistener, Listener<String> loglistener) {
    this.filter = filter;
    this.datalistener = datalistener;
    this.loglistener = loglistener;
  }

  @Override
  public void onWebSocketBinary(byte[] payload, int offset, int len) {
    throw new UnsupportedOperationException("binary payload not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void onWebSocketText(String message) {
    datalistener.process(message);
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    final Map m = HiroCollections.newMap();
    m.put("type", "close");
    m.put("code", statusCode);
    m.put("reason", reason);
    loglistener.process(JSONValue.toJSONString(m));
  }

  @Override
  public void onWebSocketConnect(Session session) {
    final Map m = HiroCollections.newMap();
    m.put("type", "connect");
    m.put("open", session.isOpen());
    loglistener.process(JSONValue.toJSONString(m));

    registerFilter(session);
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    final Map m = HiroCollections.newMap();
    m.put("type", "error");
    m.put("message", cause.getMessage());
    loglistener.process(JSONValue.toJSONString(m));
  }

  private void registerFilter(Session session) {
    final Map args = HiroCollections.newMap();
    args.put("filter-type", "jfilter");
    args.put("filter-content", filter);
    final Map f = HiroCollections.newMap();
    f.put("type", "register");
    f.put("args", args);
    try {
      session.getRemote().sendString(JSONValue.toJSONString(f));
    } catch (IOException ex) {
      Throwables.unchecked(ex);
    }
  }
}

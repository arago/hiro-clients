package co.arago.hiro.client.util;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.minidev.json.JSONValue;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;

public class SimpleWsListener implements WebSocketListener {
  private final String filter;

  public SimpleWsListener(String filter) {
    this.filter = filter;
  }

  @Override
  public void onOpen(WebSocket websocket) {
    registerFilter(websocket);
  }

  @Override
  public void onClose(WebSocket websocket, int code, String reason) {
    // blank
  }

  @Override
  public void onError(Throwable t) {
    // blank
  }

  private void registerFilter(WebSocket socket) {
    final Map args = HiroCollections.newMap();
    args.put("filter-type", "jfilter");
    args.put("filter-content", filter);
    final Map f = HiroCollections.newMap();
    f.put("type", "register");
    f.put("args", args);
    
    try {
      socket.sendTextFrame(JSONValue.toJSONString(f)).get(1, TimeUnit.MINUTES);
    } catch (Throwable ex) {
      Throwables.unchecked(ex);
    }
  }
}

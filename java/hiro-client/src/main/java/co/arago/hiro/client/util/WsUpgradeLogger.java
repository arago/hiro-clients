package co.arago.hiro.client.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.client.io.UpgradeListener;

/**
 *
 * @author fotto
 */
public class WsUpgradeLogger implements UpgradeListener {

  private static final Logger LOG = Logger.getLogger(WsUpgradeLogger.class.getName());
  private final Level debugLevel;

  public WsUpgradeLogger(Level debugLevel) {
    this.debugLevel = debugLevel != null ? debugLevel : Level.OFF;
    LOG.setLevel(this.debugLevel);
  }

  @Override
  public void onHandshakeRequest(UpgradeRequest req) {
      StringBuilder sb = new StringBuilder();
      sb.append("REQUEST [\n  Url=");
      sb.append(req.getRequestURI());
      sb.append("\n  Method=");
      sb.append(req.getMethod());
      sb.append("\n  ProtocolVersion=");
      sb.append(req.getProtocolVersion());
      sb.append("\n  SubProtocols=");
      sb.append(String.join(",", req.getSubProtocols()));
      sb.append("\n  QueryString=");
      sb.append(req.getQueryString());
      sb.append("\n  Headers:\n");
      for (Entry<String, List<String>> entry : req.getHeaders().entrySet()) {
        sb.append("    ");
        sb.append(entry.getKey());
        sb.append(": ");
        sb.append(String.join(",", entry.getValue()));
        sb.append("\n");
      }
      sb.append("  QueryParams:\n");
      for (Entry<String, List<String>> entry : req.getParameterMap().entrySet()) {
        sb.append("    ");
        sb.append(entry.getKey());
        sb.append("=");
        sb.append(String.join(",", entry.getValue()));
        sb.append("\n");
      }
      sb.append("]");
      LOG.info(sb.toString());
  }

  @Override
  public void onHandshakeResponse(UpgradeResponse resp) {
      StringBuilder sb = new StringBuilder();
      sb.append("RESPONSE [\n");
      sb.append("  Headers:\n");
      for (Entry<String, List<String>> entry : resp.getHeaders().entrySet()) {
        sb.append("    ");
        sb.append(entry.getKey());
        sb.append(": ");
        sb.append(String.join(",", entry.getValue()));
        sb.append("\n");
      }
      sb.append("  StatusCode=");
      sb.append(resp.getStatusCode());
      sb.append("\n  StatusReason=");
      sb.append(resp.getStatusReason());
      sb.append("\n  AcceptedSubProtocol=");
      sb.append(resp.getAcceptedSubProtocol());
      sb.append("\n");
      sb.append("]");
      LOG.info(sb.toString());
  }
}

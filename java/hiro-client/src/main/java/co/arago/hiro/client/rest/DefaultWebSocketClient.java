package co.arago.hiro.client.rest;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.api.WebSocketClient;
import co.arago.hiro.client.builder.ClientBuilder.WebsocketType;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import co.arago.hiro.client.util.HiroException;
import co.arago.hiro.client.util.Listener;
import co.arago.hiro.client.util.Throwables;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONValue;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;

public final class DefaultWebSocketClient implements WebSocketClient {
  private static final long PING_TIMEOUT = 30 * 1000;
  private static final int MAX_RETRIES = 5;
  public static final String DEFAULT_API_VERSION = "6.1";
  public static final String API_PREFIX = "api";
  private static final Logger LOG = Logger.getLogger(DefaultWebSocketClient.class.getName());
  
    
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private volatile WebSocket webSocketClient;
  private volatile boolean running = true;
  private int retries = 0;
  private final AtomicLong idCounter = new AtomicLong();
  private final String restApiUrl;
  private final Listener<String> loglistener;
  private final Listener<String> dataListener;
  private final AsyncHttpClient client;
  private final TokenProvider tokenProvider;
  private final int timeout;
  private final WebsocketType type;
  private final String urlParameters;

  public DefaultWebSocketClient(String restApiUrl, String urlParameters, TokenProvider tokenProvider, AsyncHttpClient client,
          Level debugLevel, int timeout, WebsocketType type, Listener<String> dataListener,
          Listener<String> loglistener) throws InterruptedException, ExecutionException, URISyntaxException {
    if (debugLevel != null) {
      LOG.setLevel(debugLevel);
    }

    this.restApiUrl = restApiUrl;
    this.tokenProvider = tokenProvider;
    this.timeout = timeout <= 0 ? 5000 : timeout;
    this.loglistener = loglistener;
    this.dataListener = dataListener;
    this.client = client;
    this.type   = type;
    this.urlParameters = urlParameters;

    connect(false);
    
    executor.scheduleWithFixedDelay(() -> ping(), PING_TIMEOUT, PING_TIMEOUT, TimeUnit.MILLISECONDS);
  }

  private void connect(boolean waitForIt) {
    if (!running) {
      return;
    }

    close0();

    if (waitForIt) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        close();

        Throwables.unchecked(ex);
      }
    }
    
    if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
      LOG.log(HiroClient.DEBUG_REST_LEVEL, "connecting " + this);
    }

    WebSocketUpgradeHandler.Builder upgradeHandlerBuilder
            = new WebSocketUpgradeHandler.Builder();

    WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder
      .addWebSocketListener(new WebSocketListener() {
        @Override
        public void onOpen(WebSocket websocket) {
          final Map m = HiroCollections.newMap();
          m.put("type", "open");
          m.put("open", websocket.isOpen());

          if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
            LOG.log(HiroClient.DEBUG_REST_LEVEL, "connected " + this);
          }

          process(loglistener, JSONValue.toJSONString(m));
        }

        @Override
        public void onClose(WebSocket websocket, int code, String reason) {
          if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
            LOG.log(HiroClient.DEBUG_REST_LEVEL, "received close " + this);
          }

          if (running) {
            connect(false);
          } else {
            final Map m = HiroCollections.newMap();
            m.put("type", "close");
            m.put("code", code);
            m.put("reason", reason);

            process(loglistener, JSONValue.toJSONString(m));
          }
        }

        @Override
        public void onError(Throwable t) {
          if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
            LOG.log(HiroClient.DEBUG_REST_LEVEL, "received error " + this, t);
          }

          if (running) {
            connect(false);
          } else {
            final Map m = HiroCollections.newMap();
            m.put("type", "error");
            m.put("message", t.getMessage());

            process(loglistener, JSONValue.toJSONString(m));
          }
        }

        @Override
        public void onTextFrame(String payload, boolean finalFragment, int rsv) {
          if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
            LOG.log(HiroClient.DEBUG_REST_LEVEL, "received message " + payload);
          }

          process(dataListener, payload);
        }

        @Override
        public void onPingFrame(byte[] payload) {
          if (webSocketClient != null && webSocketClient.isOpen()) webSocketClient.sendPongFrame(payload);
        }
      }).build();
    
    try {
      webSocketClient = client
        .prepareGet(composeWsUrl(restApiUrl))
        .addHeader("Sec-WebSocket-Protocol", getProtocol() + ", token-" + tokenProvider.getToken())
        .setRequestTimeout(timeout)
        .execute(wsHandler)
        .get(timeout, TimeUnit.MILLISECONDS);
    } catch (Throwable ex) {
      close();
      
      if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
        LOG.log(HiroClient.DEBUG_REST_LEVEL, "connection failed " + this, ex);
      }
      
      throw new HiroException("connection failed " + this + " " + ex.getMessage(), 400, ex);
    }
    
    if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
      LOG.log(HiroClient.DEBUG_REST_LEVEL, "connection opened " + this);
    }
  }

  private void process(Listener<String> listener, String payload) {
    try {
      listener.process(payload);
    } catch (Throwable t) {
      LOG.log(Level.WARNING, listener + " failed", t);
    }
  }
  
  @Override
  public synchronized void sendMessage(String message) {
    if (webSocketClient == null || !webSocketClient.isOpen()) {
      connect(false);
    }

    if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
      LOG.log(HiroClient.DEBUG_REST_LEVEL, "send message " + message);
    }

    try {
      webSocketClient.sendTextFrame(message).get(timeout, TimeUnit.MILLISECONDS);
      retries = 0;
    } catch (Throwable ex) {
      if (retries < MAX_RETRIES) {
        LOG.log(Level.WARNING, "send failed, retrying", ex);

        ++retries;
        connect(true);

        sendMessage(message);
      } else {
        close();

        Throwables.unchecked(ex);
      }
    }
  }

  @Override
  public synchronized long sendMessage(String type, Map<String, String> headers, Map body) {
    final long id = idCounter.incrementAndGet();

    final Map request = HiroCollections.newMap();
    request.put("id", id);
    request.put("type", type);
    request.put("headers", headers);
    request.put("body", body);
    
    sendMessage(Helper.composeJson(request));
    
    return id;
  }

  @Override
  public synchronized void close() {
    running = false;

    close0();
  }
  
  private void close0()
  {
    if (webSocketClient != null && webSocketClient.isOpen()) {
      if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
        LOG.log(HiroClient.DEBUG_REST_LEVEL, "closing " + this);
      }
      
      try {
        webSocketClient.sendCloseFrame(200, "OK").get(timeout, TimeUnit.MILLISECONDS);
      } catch (Throwable ignored) {
        // blank
      }
    }

    webSocketClient = null;
    
    executor.shutdownNow();
  }

  private String composeWsUrl(String inUrl) {
    try {
      URI uri = new URI(inUrl);
      StringBuilder sb = new StringBuilder();
      if ("http".equals(uri.getScheme())) {
        sb.append("ws://");
      } else if ("https".equals(uri.getScheme())) {
        sb.append("wss://");
      } else {
        sb.append(uri.getScheme());
        sb.append("://");
      }
      sb.append(uri.getHost());
      if (uri.getPort() > 0) {
        sb.append(":");
        sb.append(uri.getPort());
      }
      if (uri.getPath().isEmpty()) {
        sb.append("/");
        sb.append(API_PREFIX);
        sb.append("/");
        
        switch(type) {
          case Event:
            sb.append("event-ws");
          break;

          case Graph:
            sb.append("graph-ws");
          break;
          
          default:
            throw new IllegalArgumentException("unknown type " + type);
        }
        
        sb.append("/");
        sb.append(DEFAULT_API_VERSION);
        
        if (urlParameters != null && !urlParameters.isEmpty())
        {
          sb.append("?");
          sb.append(urlParameters);
        }
      }
      return sb.toString();
    } catch (URISyntaxException ex) {
      throw new RuntimeException(inUrl, ex);
    }
  }

  @Override
  public String toString() {
    return "DefaultWebSocketClient{" + "running=" + running + ", retries=" + retries + ", restApiUrl=" + restApiUrl + ", timeout=" + timeout + ", type=" + type + '}';
  }
  
  private String getProtocol() {
    switch(type)
    {
      case Event:
        return "events-1.0.0";

      case Graph:
        return "graph-2.0.0";

      default:
        throw new IllegalArgumentException("unknown type " + type);
    }
  }
  
  private void ping()
  {
    if (webSocketClient != null && webSocketClient.isOpen()) {
      try {
        webSocketClient.sendPingFrame().get(timeout, TimeUnit.MILLISECONDS);
      } catch (Throwable t) {
        final Map m = HiroCollections.newMap();
        m.put("type", "error");
        m.put("message", "ping failed " + t.getMessage());

        process(loglistener, JSONValue.toJSONString(m));
      }
    }
  }
}

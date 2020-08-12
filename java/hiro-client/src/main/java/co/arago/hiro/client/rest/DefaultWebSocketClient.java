package co.arago.hiro.client.rest;

import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.api.WebSocketClient;
import co.arago.hiro.client.builder.ClientBuilder.WebsocketType;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import co.arago.hiro.client.util.HiroException;
import co.arago.hiro.client.util.Listener;
import co.arago.hiro.client.util.Throwables;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONValue;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;

public final class DefaultWebSocketClient implements WebSocketClient {
    private static final long PING_TIMEOUT = 30 * 1000;
    private static final int MAX_RETRIES = 5;
    public static final String DEFAULT_API_VERSION = "6.1";
    public static final String ACTION_API_VERSION = "1.0";
    public static final String API_PREFIX = "api";
    private static final Logger LOG = Logger.getLogger(DefaultWebSocketClient.class.getName());

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private volatile WebSocket webSocketClient;
    private volatile boolean running = false;
    private volatile boolean tokenValid = false;
    private int retries = 0;
    private final AtomicLong idCounter = new AtomicLong();
    private final String restApiUrl;
    private final Listener<String> logListener;
    private final Listener<String> dataListener;
    private AsyncHttpClient client;
    private final TokenProvider tokenProvider;
    private final int timeout;
    private final WebsocketType type;
    private final String urlParameters;
    private final WebSocketListener handler;

    private final Map<String, Map> eventFilterMessages = new ConcurrentHashMap<>();

    /**
     * This implementation ensures that {@link #reconnect()} is not triggered while the websocket is currently
     * reconnection to avoid recursive calls of the method.
     */
    private class DefaultWebSocketListener implements WebSocketListener {

        /**
         * Flag to prevent recursive calls to {@link #reconnect()}.
         */
        private volatile boolean doReconnect;

        /**
         * Setting this is the only way to avoid reconnecting an existing connection when a close event comes in. It
         * gets set when the token for the connection is unrecoverably invalid as per error message received in
         * {@link #onTextFrame(String, boolean, int)}.
         */
        private volatile boolean exitOnClose = false;

        /**
         * This flag gets set when renewing a token throws an exception. The subsequent call to
         * {@link #onError(Throwable)} will then close the websocket.
         */
        private volatile boolean exitOnError = false;

        /**
         * Constructor
         * 
         * @param isReconnecting
         *            Will be set inside {@link #connect(boolean)}. Set {@link #doReconnect} only if isReconnecting is
         *            false.
         */
        public DefaultWebSocketListener(boolean isReconnecting) {
            this.doReconnect = !isReconnecting;
        }

        /**
         * Invoked when the {@link WebSocket} is open.<br/>
         * Sets {@link #doReconnect} to 'true' because the websocket is now connected again.
         *
         * @param websocket
         *            the WebSocket
         */
        @Override
        public void onOpen(WebSocket websocket) {
            final Map m = HiroCollections.newMap();
            m.put("type", "open");
            m.put("open", websocket.isOpen());

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "connected " + this);
            }

            if (handler != null) {
                handler.onOpen(websocket);
            }

            process(logListener, JSONValue.toJSONString(m));
            doReconnect = true;
            tokenValid = false;
            exitOnClose = false;
            exitOnError = false;
        }

        /**
         * Invoked when the {@link WebSocket} is closed.
         *
         * @param websocket
         *            the WebSocket
         * @param code
         *            the status code
         * @param reason
         *            the reason message
         * @see "http://tools.ietf.org/html/rfc6455#section-5.5.1"
         */
        @Override
        public void onClose(WebSocket websocket, int code, String reason) {
            final Map m = HiroCollections.newMap();
            m.put("type", "close");
            m.put("code", code);
            m.put("reason", reason);

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "received close " + this);
            }

            if (handler != null) {
                handler.onClose(websocket, code, reason);
            }

            process(logListener, JSONValue.toJSONString(m));

            if (exitOnClose) {
                if (running) {
                    close();
                }
            } else if (doReconnect) {
                reconnect();
            }
        }

        /**
         * Invoked when the {@link WebSocket} crashes.
         *
         * @param t
         *            a {@link Throwable}
         */
        @Override
        public void onError(Throwable t) {
            final Map m = HiroCollections.newMap();
            m.put("type", "error");
            m.put("message", t.getMessage());
            m.put("stack", stacktrace(t));

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "received error " + this, t);
            }

            if (handler != null) {
                handler.onError(t);
            }

            process(logListener, JSONValue.toJSONString(m));

            if (exitOnError) {
                exitOnClose = true;
                if (running) {
                    close();
                }
            } else if (doReconnect) {
                reconnect();
            }
        }

        /**
         * For incoming messages. This also detects 401 error messages from the other side and handles token updates and
         * reconnection, or setting {@link #exitOnClose} when the token remains invalid or {@link #exitOnError} when
         * renewing the token throws an exception.
         *
         * @param payload
         *            Incoming message as String
         * @param finalFragment
         *            For partial messages
         * @param rsv
         *            Extension bits
         */
        @Override
        public void onTextFrame(String payload, boolean finalFragment, int rsv) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "received message " + payload);
            }

            if (handler != null) {
                handler.onTextFrame(payload, finalFragment, rsv);
            }

            Object o = JSONValue.parse(payload);
            if (o instanceof Map && ((Map) o).containsKey("error")) {
                Map error = (Map) ((Map) o).get("error");
                if ((int) error.get("code") == 401) {
                    if (tokenValid) {
                        try {
                            tokenValid = false;
                            tokenProvider.renewToken();
                            reconnect();
                        } catch (Throwable t) {
                            exitOnError = true;
                            onError(t);
                        }
                    } else {
                        exitOnClose = true;
                        doReconnect = false;
                        process(dataListener, payload);
                    }
                    return;
                }
            }

            // Token is valid when no error 401 came in.
            tokenValid = true;

            process(dataListener, payload);
        }

        @Override
        public void onPingFrame(byte[] payload) {
            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.sendPongFrame(payload);
            }
        }
    }

    public DefaultWebSocketClient(String restApiUrl, String urlParameters, TokenProvider tokenProvider,
            AsyncHttpClient client, Level debugLevel, int timeout, WebsocketType type, Listener<String> dataListener,
            Listener<String> logListener, WebSocketListener handler, List<Map> eventFilterMessages)
            throws InterruptedException, ExecutionException, URISyntaxException {

        if (debugLevel != null) {
            LOG.setLevel(debugLevel);
        }

        this.restApiUrl = restApiUrl;
        this.tokenProvider = tokenProvider;
        this.timeout = timeout <= 0 ? 5000 : timeout;
        this.logListener = logListener;
        this.dataListener = dataListener;
        this.client = client;
        this.type = type;
        this.urlParameters = urlParameters;
        this.handler = handler;

        if (eventFilterMessages != null) {
            for (Map filter : eventFilterMessages) {
                this.eventFilterMessages.put(getFilterId(filter), filter);
            }
        }

        connect(false);

        executor.scheduleWithFixedDelay(() -> ping(), PING_TIMEOUT, PING_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * Setup the websocket and connect.
     */
    private void connect(boolean isReconnecting) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "connecting " + this);
        }

        WebSocketUpgradeHandler wsHandler = new WebSocketUpgradeHandler.Builder()
                .addWebSocketListener(new DefaultWebSocketListener(isReconnecting)).build();

        try {
            webSocketClient = client.prepareGet(composeWsUrl(restApiUrl))
                    .addHeader("Sec-WebSocket-Protocol", getProtocol() + ", token-" + tokenProvider.getToken())
                    .setRequestTimeout(timeout).execute(wsHandler).get(timeout, TimeUnit.MILLISECONDS);

            if (webSocketClient == null) {
                throw new HiroException("Failed to initialize WebSocketClient. It is null.", 500);
            }

            if (type == WebsocketType.Event) {
                for (Map filter : eventFilterMessages.values()) {
                    String message = getEventRegisterMessage(filter);
                    LOG.log(Level.INFO, "Send filter: " + message);
                    webSocketClient.sendTextFrame(message).get(timeout, TimeUnit.MILLISECONDS);
                }
            }

            running = true;
        } catch (Throwable ex) {
            closeWs();

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "connection failed " + this, ex);
            }

            throw new HiroException("connection failed " + this + " " + ex.getMessage(), 400, ex);
        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "connection opened " + this);
        }
    }

    /**
     * Reconnect a connection that should not have been closed. This also implements a delay strategy for repeated
     * attempts to reconnect as suggested per RFC6455. Reconnect attempts only stop on {@link #close()} or an
     * InterruptedException while sleeping.
     */
    private synchronized void reconnect() {
        if (!running) {
            return;
        }

        Duration nextTryDelay = Duration.ZERO;

        closeWs();

        while (true) {
            try {
                long delay = nextTryDelay.getSeconds() * 1000;
                if (delay > 0)
                    wait(delay);

                if (!running) {
                    return;
                }

                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "Reconnecting " + this);
                }

                connect(true);

                return;
            } catch (HiroException ex) {

                LOG.log(Level.SEVERE, "Reconnect caught exception.", ex);

                // Retry strategy
                if (nextTryDelay.getSeconds() < 3) {
                    nextTryDelay = nextTryDelay.plusSeconds(1);
                } else if (nextTryDelay.getSeconds() < 60) {
                    nextTryDelay = nextTryDelay.plusSeconds(new Random().nextInt(10) + 1);
                } else {
                    nextTryDelay = Duration.ofMinutes(new Random().nextInt(10) + 1);
                }

                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "Retrying connection after " + nextTryDelay.getSeconds() + "s.");
                }

            } catch (InterruptedException ex) {
                closeWs();
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Reconnect interrupted " + this);
                }
            }
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
        if (!running) {
            throw new HiroException("connection closed", 400);
        }

        if (webSocketClient == null || !webSocketClient.isOpen()) {
            reconnect();
        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "send message " + message);
        }

        try {
            webSocketClient.sendTextFrame(message).get(timeout, TimeUnit.MILLISECONDS);
            retries = 0;
        } catch (Throwable ex) {
            if (running && webSocketClient.isOpen() && retries < MAX_RETRIES) {
                LOG.log(Level.WARNING, "send failed, retrying", ex);

                ++retries;
                reconnect();

                sendMessage(message);
            } else {
                closeWs();
                Throwables.unchecked(ex);
            }
        }
    }

    @Override
    public synchronized void addEventFilter(Map filter) {
        String id = getFilterId(filter);
        String message = getEventRegisterMessage(filter);
        LOG.log(Level.INFO, "Add filter: " + message);
        sendMessage(message);
        eventFilterMessages.put(id, filter);
    }

    private String getFilterId(Map filter) {
        String id = (String) filter.get("filter-id");
        if (StringUtils.isEmpty(id)) {
            throw new HiroException("Wrong filter specification. Key 'filter-id' is missing.", 400);
        }
        return id;
    }

    private String getEventRegisterMessage(Map filter) {
        final Map m = HiroCollections.newMap();
        m.put("type", "register");
        m.put("args", filter);

        String message = JSONValue.toJSONString(m);

        return message;
    }

    @Override
    public synchronized void removeEventFilter(String id) {
        final Map m = HiroCollections.newMap();
        m.put("type", "unregister");
        m.put("args", HiroCollections.newMap("filter-id", id));

        String message = JSONValue.toJSONString(m);

        LOG.log(Level.INFO, "Remove filter: " + message);

        sendMessage(message);
        eventFilterMessages.remove(id);
    }

    @Override
    public synchronized void clearEventFilters() {
        final Map m = HiroCollections.newMap();
        m.put("type", "clear");
        m.put("args", HiroCollections.newMap());

        String message = JSONValue.toJSONString(m);

        LOG.log(Level.INFO, "Clear filter: " + message);

        sendMessage(message);

        eventFilterMessages.clear();
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

        notifyAll();
        closeWs();

        executor.shutdownNow();
    }

    private void closeWs() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "closing " + this);
            }

            try {
                webSocketClient.sendCloseFrame(200, "OK").get(timeout, TimeUnit.MILLISECONDS);
            } catch (Throwable ignored) {
                // blank
            }
        }

        webSocketClient = null;
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

                switch (type) {
                case Event:
                    sb.append("events-ws/" + DEFAULT_API_VERSION);
                    break;

                case Graph:
                    sb.append("graph-ws/" + DEFAULT_API_VERSION);
                    break;

                case Action:
                    sb.append("action-ws/" + ACTION_API_VERSION);
                    break;

                default:
                    throw new IllegalArgumentException("unknown type " + type);
                }

                if (urlParameters != null && !urlParameters.isEmpty()) {
                    sb.append(urlParameters.startsWith("?") ? "" : "?");
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
        return "DefaultWebSocketClient{" + "running=" + running + ", retries=" + retries + ", restApiUrl=" + restApiUrl
                + ", timeout=" + timeout + ", type=" + type + '}';
    }

    private String getProtocol() {
        switch (type) {
        case Event:
            return "events-1.0.0";

        case Graph:
            return "graph-2.0.0";

        case Action:
            return "action-1.0.0";

        default:
            throw new IllegalArgumentException("unknown type " + type);
        }
    }

    private void ping() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            try {
                webSocketClient.sendPingFrame().get(timeout, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                final Map m = HiroCollections.newMap();
                m.put("type", "error");
                m.put("message", "ping failed " + t.getMessage());
                m.put("stack", stacktrace(t));

                process(logListener, JSONValue.toJSONString(m));
            }
        }
    }

    private String stacktrace(Throwable t) {
        if (t == null) {
            return "";
        }

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}

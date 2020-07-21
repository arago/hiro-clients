package co.arago.hiro.client.builder;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.api.RestClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.api.WebSocketClient;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.rest.DefaultHiroClient;
import co.arago.hiro.client.rest.DefaultWebSocketClient;
import co.arago.hiro.client.util.HttpClientHelper;
import co.arago.hiro.client.util.Listener;
import co.arago.hiro.client.util.Throwables;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ws.WebSocketListener;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static co.arago.hiro.client.util.Helper.notEmpty;
import static co.arago.hiro.client.util.Helper.notNull;

public class ClientBuilder {

    protected String restApiUrl;
    protected TokenProvider tokenProvider;
    protected AsyncHttpClient client;
    protected boolean trustAllCerts;
    protected Level debugLevel = Level.OFF;
    protected int timeout = 0; // msecs
    protected List<Map> eventFilterMessages;

    public enum WebsocketType {
        Graph, Event
    }

    public ClientBuilder setRestApiUrl(String restApiUrl) {
        this.restApiUrl = restApiUrl;
        return this;
    }

    public ClientBuilder setTokenProvider(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        return this;
    }

    public ClientBuilder setClient(AsyncHttpClient client) {
        this.client = client;
        return this;
    }

    public ClientBuilder setTrustAllCerts(boolean trustAllCerts) {
        this.trustAllCerts = trustAllCerts;
        return this;
    }

    public ClientBuilder setDebugRest(Level debugLevel) {
        this.debugLevel = debugLevel;
        return this;
    }

    public ClientBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ClientBuilder setEventFilterMessages(List<Map> eventFilterMessages) {
        this.eventFilterMessages = eventFilterMessages;
        return this;
    }

    public HiroClient makeHiroClient() {
        return new DefaultHiroClient(notEmpty(restApiUrl, "restApiUrl"), notNull(tokenProvider, "tokenProvider"),
                client, trustAllCerts, debugLevel, timeout);
    }

    public RestClient makeRestClient() {
        return new AuthenticatedRestClient(notEmpty(restApiUrl, "restApiUrl"), notNull(tokenProvider, "tokenProvider"),
                client, trustAllCerts, debugLevel, timeout);
    }

    public WebSocketClient makeWebSocketClient(Listener<String> dataListener, Listener<String> loglistener) {
        return makeWebSocketClient(WebsocketType.Graph, "", dataListener, loglistener);
    }

    public WebSocketClient makeWebSocketClient(WebsocketType type, String urlParameters, Listener<String> dataListener,
            Listener<String> logListener) {
        return makeWebSocketClient(type, urlParameters, dataListener, logListener, null);
    }

    public WebSocketClient makeWebSocketClient(WebsocketType type, String urlParameters, Listener<String> dataListener,
            Listener<String> logListener, WebSocketListener handler) {
        if (client == null) {
            client = HttpClientHelper.newClient(trustAllCerts, this.timeout);
        }

        try {
            return new DefaultWebSocketClient(restApiUrl, urlParameters, tokenProvider, client, debugLevel, timeout,
                    type, dataListener, logListener, handler, eventFilterMessages);
        } catch (Throwable ex) {
            return Throwables.unchecked(ex);
        }
    }
}

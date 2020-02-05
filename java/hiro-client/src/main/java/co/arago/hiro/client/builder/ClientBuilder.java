package co.arago.hiro.client.builder;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.api.RestClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.api.WebSocketClient;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.rest.DefaultHiroClient;
import co.arago.hiro.client.rest.DefaultWebSocketClient;
import static co.arago.hiro.client.util.Helper.*;
import co.arago.hiro.client.util.HttpClientHelper;
import co.arago.hiro.client.util.Listener;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.asynchttpclient.AsyncHttpClient;

public class ClientBuilder {

  protected String restApiUrl;
  protected TokenProvider tokenProvider;
  protected AsyncHttpClient client;
  protected boolean trustAllCerts;
  protected Level debugLevel;
  protected int timeout = 0; // msecs

  public enum WebsocketType {
    /**
     * https://docs.hiro.arago.co/hiro/6.2.0/user/hiro-graph-api/ws-api.html#ws-event-streaming-api
     */
    Graph,
    /**
     * https://docs.hiro.arago.co/hiro/6.2.0/user/hiro-graph-api/ws-api.html#ws-event-streaming-api
     */
    Event
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

  public HiroClient makeHiroClient() {
    return new DefaultHiroClient(notEmpty(restApiUrl, "restApiUrl"),
      notNull(tokenProvider, "tokenProvider"), client,
      trustAllCerts, debugLevel, timeout);
  }

  public RestClient makeRestClient() {
    return new AuthenticatedRestClient(notEmpty(restApiUrl, "restApiUrl"),
      notNull(tokenProvider, "tokenProvider"), client,
      trustAllCerts, debugLevel, timeout);
  }
  
  public WebSocketClient makeWebSocketClient(Listener<String> dataListener, Listener<String> loglistener)
          throws InterruptedException, ExecutionException, URISyntaxException {
    if (client == null) {
      client =  HttpClientHelper.newClient(trustAllCerts, this.timeout);
    }
    return new DefaultWebSocketClient(restApiUrl, "", tokenProvider, client, debugLevel, timeout, WebsocketType.Graph, dataListener, loglistener);
  }
  
  /**
   * 
   * @param type the type of the websocket, graph or event
   * @param urlParameters extra properly encoded url parameters without the ?
   * @param dataListener listener to process messages received
   * @param loglistener listener to process websocket lifecycle events, like open, close
   * @return
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws URISyntaxException 
   */
  public WebSocketClient makeWebSocketClient(WebsocketType type, String urlParameters, Listener<String> dataListener, Listener<String> loglistener)
          throws InterruptedException, ExecutionException, URISyntaxException {
    if (client == null) {
      client =  HttpClientHelper.newClient(trustAllCerts, this.timeout);
    }
    return new DefaultWebSocketClient(restApiUrl, urlParameters, tokenProvider, client, debugLevel, timeout, type, dataListener, loglistener);
  }
}

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
  protected Level debugLevel = Level.OFF;
  protected int timeout = 0; // msecs
  protected String apiVersion = null; // enforce  /api/<vers>/graph

  public enum WebsocketType {
    Graph,
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

  /**
   * allows to set externally created AsyncHttpClient
   *
   * usually not required since a missing client will
   * be created automatically.
   *
   * if used the caller is responsible
   * for proper initialization, e.g. setting timeouts
   *
   * @param client
   * @return
   */
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

  public ClientBuilder setApiVersion(String version) {
    this.apiVersion = version;

    return this;
  }

  public HiroClient makeHiroClient() {
    return new DefaultHiroClient(notEmpty(restApiUrl, "restApiUrl"),
      notNull(tokenProvider, "tokenProvider"), client,
      trustAllCerts, debugLevel, timeout, apiVersion);
  }

  public RestClient makeRestClient() {
    return new AuthenticatedRestClient(notEmpty(restApiUrl, "restApiUrl"),
      notNull(tokenProvider, "tokenProvider"), client,
      trustAllCerts, debugLevel, timeout, null);
  }

  public WebSocketClient makeWebSocketClient(Listener<String> dataListener, Listener<String> loglistener)
          throws InterruptedException, ExecutionException, URISyntaxException {
    return makeWebSocketClient(WebsocketType.Graph, "", dataListener, loglistener);
  }
  
  public WebSocketClient makeWebSocketClient(WebsocketType type, String urlParameters, Listener<String> dataListener, Listener<String> loglistener)
          throws InterruptedException, ExecutionException, URISyntaxException {
    if (client == null) {
      client =  HttpClientHelper.newClient(trustAllCerts, this.timeout);
    }
    return new DefaultWebSocketClient(restApiUrl, urlParameters, tokenProvider, client, debugLevel, timeout, type, dataListener, loglistener);
  }
}

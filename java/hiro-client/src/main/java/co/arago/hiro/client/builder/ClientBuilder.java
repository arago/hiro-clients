package co.arago.hiro.client.builder;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.api.RestClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.rest.DefaultHiroClient;
import java.util.logging.Level;
import org.asynchttpclient.AsyncHttpClient;

import static co.arago.hiro.client.util.Helper.*;

public class ClientBuilder {

  protected String restApiUrl;
  protected TokenProvider tokenProvider;
  protected AsyncHttpClient client;
  protected boolean trustAllCerts;
  protected Level debugLevel;
  protected int timeout = 0; // msecs

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
}

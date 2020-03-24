package co.arago.hiro.client.auth;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import static co.arago.hiro.client.util.Helper.*;
import co.arago.hiro.client.util.HiroCollections;
import co.arago.hiro.client.util.HiroException;
import co.arago.hiro.client.util.HttpClientHelper;
import co.arago.hiro.client.util.Throwables;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONValue;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;

/**
 *
 */
public abstract class AbstractTokenProvider implements TokenProvider, Closeable {

  public static final String DEFAULT_API_VERSION = "6.1";
  public static final String API_PREFIX = "/api";
  public static final String API_SUFFIX = "auth";

  private static final Logger LOG = Logger.getLogger(AbstractTokenProvider.class.getName());
  protected static final String REFRESH_TOKEN = "refresh_token";
  protected static final String ACCESS_TOKEN = "_TOKEN";
  protected static final String EXPIRES_IN = "expires_in";
  private static final String CLIENT_SECRET = "client_secret";
  private static final String CLIENT_ID = "client_id";

  private final int timeout = 60000;
  private final int invalidateTokenPeriod = 5000;
  private final int invalidateRefreshTokenPeriod = 300000;

  private final AsyncHttpClient client;
  private final String url;
  private final String apiUrl;
  protected final String clientSecret;
  protected final String clientId;

  private volatile String currentToken;
  private String refreshToken;
  private long invalidAfter = -1;

  public AbstractTokenProvider(String url, String clientId, String clientSecret,
    AsyncHttpClient client, boolean trustAllCerts) {
    this(url, clientId, clientSecret, client, trustAllCerts, null, null);
  }

  public AbstractTokenProvider(String url, String clientId, String clientSecret,
    AsyncHttpClient client, boolean trustAllCerts, Level debugLevel, String apiVersion) {
    this.url = notEmpty(url, "url").replaceAll("/+$", "");
    this.clientId = notEmpty(clientId, "clientId");
    this.clientSecret = notEmpty(clientSecret, "clientSecret");
    this.client = client == null ? HttpClientHelper.newClient(trustAllCerts) : client;
    if (debugLevel != null) {
      LOG.setLevel(debugLevel);
    }
    if (apiVersion != null && !apiVersion.isEmpty()) {
      this.apiUrl = StringUtils.join(HiroCollections.newList(API_PREFIX, API_SUFFIX, apiVersion), "/");
    } else {
      this.apiUrl = StringUtils.join(HiroCollections.newList(API_PREFIX, API_SUFFIX, DEFAULT_API_VERSION), "/");
    }
  }

  private synchronized void obtainToken() {
    final Map data = new HashMap();
    prepareTokenRequest(data);
    final BoundRequestBuilder builder = newRequest(data, apiUrl + "/app");

    try {
      if (LOG.isLoggable(Level.FINEST)) {
        HttpClientHelper.debugRequest(builder.build(), LOG, Level.FINEST);
      }

      final Response response = checkResponse(builder.execute().get(timeout, TimeUnit.MILLISECONDS));

      process(parseResponse(response));
    } catch (Exception ex) {
      Throwables.unchecked(ex);
    }
  }

  protected abstract void prepareTokenRequest(Map data);

  @Override
  public boolean checkTokenRenewal(Response response) {
    return checkTokenRenewal(response.getStatusCode());
  }

  @Override
  public boolean checkTokenRenewal(int httpResponseCode) {
    if (httpResponseCode == 401) {
      renewToken();
      return true;
    }

    return false;
  }

  @Override
  public void renewToken() {
    resetTokenState();
    getToken();
  }

  @Override
  public void revokeToken() {
    revokeToken(true);
  }

  @Override
  public void revokeToken(boolean resetState) {
    final Map data = new HashMap();
    BoundRequestBuilder builder = newRequest(data, apiUrl + "/revoke");
    builder.addHeader("Authorization", "Bearer " + currentToken);

    Response response;
    try {
      if (LOG.isLoggable(Level.FINEST)) {
        HttpClientHelper.debugRequest(builder.build(), LOG, Level.FINEST);
      }

      response = checkResponse(builder.execute().get(timeout, TimeUnit.MILLISECONDS));
      if (resetState) {
        resetTokenState();
      }

    } catch (Exception e) {
      Throwables.unchecked(e);
    }
  }

  @Override
  public void resetTokenState() {
    currentToken = null;
    refreshToken = null;
  }

  private synchronized void refreshToken() {
    if (refreshToken == null || refreshToken.isEmpty()) {
      obtainToken();
      return;
    }

    final Map data = new HashMap();
    data.put(REFRESH_TOKEN, refreshToken);
    final BoundRequestBuilder builder = newRequest(data, apiUrl + "/refresh");

    try {
      final Response response = checkResponse(builder.execute().get(timeout, TimeUnit.MILLISECONDS));

      process(parseResponse(response));
    } catch (Exception ex) {
      obtainToken();
    }
  }

  @Override
  public void close() throws IOException {
    if (client != null) {
      client.close();
    }
  }

  private Response checkResponse(final Response response) {
    if (LOG.isLoggable(Level.FINEST)) {
      HttpClientHelper.debugResponse(response, LOG, Level.FINEST);
    }

    if (response.getStatusCode() != 200) {
      if (response.getStatusCode() == 400 || response.getStatusCode() == 403) {
        throw new HiroException("upstream error: " + AuthenticatedRestClient.tryUnwrap(response.getResponseBody(), response.getStatusText()), response.getStatusCode());
      } else {
        throw new HiroException("upstream error: " + response.getStatusText(), response.getStatusCode());
      }
    }
    return response;
  }

  private void process(Map map) {
    if (map.get(EXPIRES_IN) != null) {
      this.invalidAfter = System.currentTimeMillis() + (((int) map.get(EXPIRES_IN)) * 1000);
    } else {
      this.invalidAfter = -1;
    }

    this.currentToken = notEmpty((String) map.get(ACCESS_TOKEN), "upstream " + ACCESS_TOKEN);
    this.refreshToken = (String) map.get(REFRESH_TOKEN);
  }

  @Override
  public final String toString() {
    return getToken();
  }

  @Override
  public final String getToken() {
    if (currentToken == null) {
      obtainToken();
    } else if (shouldRefresh()) {
      refreshToken();
    }

    return notEmpty(currentToken, "could not obtain token");
  }

  private Map parseResponse(Response response) throws Exception {
    final Object parsed = JSONValue.parse(response.getResponseBodyAsStream());
    // TODO check 503 statuscode
    if (!(parsed instanceof Map)) {
      throw new HiroException("invalid upstream response " + parsed, 503);
    }

    return (Map) parsed;
  }

  private boolean shouldRefresh() {
    if (invalidAfter < 0) {
      return false;
    }

    long now = System.currentTimeMillis();

    if (refreshToken != null && now >= invalidAfter - invalidateRefreshTokenPeriod) {
      return true;
    }

    return (now >= invalidAfter - invalidateTokenPeriod);
  }

  private BoundRequestBuilder newRequest(Map data, String path) {

    data.put(CLIENT_ID, clientId);
    data.put(CLIENT_SECRET, clientSecret);

    return client.preparePost(url + path)
      .setHeader("Accept", "application/json")
      .setHeader("Content-type", "application/json;charset=UTF-8")
      .setBody(JSONValue.toJSONString(data));
  }
}

package co.arago.hiro.client.rest;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.api.RestClient;
import static co.arago.hiro.client.api.RestClient.CONTENT_TYPE_DEFAULT;
import static co.arago.hiro.client.api.RestClient.CONTENT_TYPE_OCTECT_STREAM;
import static co.arago.hiro.client.api.RestClient.DEFAULT_ENCODING;
import static co.arago.hiro.client.api.RestClient.HEADER_ACCEPT;
import static co.arago.hiro.client.api.RestClient.HEADER_CONTENT_TYPE;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.auth.FixedTokenProvider;
import static co.arago.hiro.client.util.Helper.notEmpty;
import static co.arago.hiro.client.util.Helper.notNull;
import co.arago.hiro.client.util.HiroCollections;
import co.arago.hiro.client.util.HiroException;
import co.arago.hiro.client.util.HttpClientHelper;
import co.arago.hiro.client.util.Listener;
import co.arago.hiro.client.util.Throwables;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONValue;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.jsfr.json.JsonPathListener;
import org.jsfr.json.JsonSurferJackson;
import org.jsfr.json.NonBlockingParser;
import org.jsfr.json.ParsingContext;
import org.jsfr.json.SurfingConfiguration;

public class AuthenticatedRestClient implements RestClient {

  private static final Logger LOG = Logger.getLogger(AuthenticatedRestClient.class.getName());
  private static final int MAX_TRIES = 25;
  private static final int TIMEOUT = 10 * 60 * 1000;
  private final String restApiUrl;
  private final String apiPath;
  private final int timeout;
  private final AsyncHttpClient client;
  private final Level debugRestLevel;
  private final TokenProvider tokenProvider;
  private final boolean trustAllCerts;
  public static final List REDIRECT_CODES = HiroCollections.newList(301, 302, 303, 307, 308);

  public AuthenticatedRestClient(String restApiUrl, TokenProvider tokenProvider, boolean trustAllCerts, Level debugLevel) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, 0, null);
  }

  public AuthenticatedRestClient(String restApiUrl, TokenProvider tokenProvider, boolean trustAllCerts, Level debugLevel, int timeout) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, timeout, null);
  }

  // if client is set then trustAllCerts and timeout are already set
  public AuthenticatedRestClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client, Level debugLevel) {
    this(restApiUrl, tokenProvider, client, false, debugLevel, 0, null);
  }

  // still needed for ClientBuilder => public
  public AuthenticatedRestClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client, boolean trustAllCerts, Level debugLevel, int timeout, String apiPath) {
    this.timeout = timeout > 0 ? timeout : TIMEOUT;
    this.restApiUrl = notEmpty(restApiUrl, "restApiUrl").endsWith("/") ? restApiUrl.substring(0, restApiUrl.length() - 1) : restApiUrl;
    this.client = client == null ? HttpClientHelper.newClient(trustAllCerts, this.timeout) : client;
    this.debugRestLevel = debugLevel != null ? debugLevel : Level.OFF;
    LOG.setLevel(this.debugRestLevel);
    this.tokenProvider = notNull(tokenProvider, "tokenProvider");
    if (apiPath != null && !apiPath.isEmpty()) {
      this.apiPath = apiPath;
    } else {
      this.apiPath = "";
    }
    this.trustAllCerts = trustAllCerts;
  }

  @Override
  public void get(List<String> path, Map<String, String> params, Listener<Map> listener) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1} params: {2}",
        new Object[]{"GET", composeUrl(path), params});
    }
    BoundRequestBuilder prepareGet
      = client().prepareGet(composeUrl(path));
    runAsyncRequest(prepareGet, params, listener, true);
  }

  @Override
  public String get(List<String> path, Map<String, String> params) {
    return this.get(path, null, params);
  }

  @Override
  public String get(List<String> path, String body, Map<String, String> params) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1} params: {2}",
        new Object[]{"GET", composeUrl(path), params});
    }
    BoundRequestBuilder prepareGet
      = client().prepareGet(composeUrl(path));
    return runRequest(prepareGet, body, params);
  }

  @Override
  public String get(String path, Map<String, String> params) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1} params: {2}",
        new Object[]{"GET", path, params});
    }
    BoundRequestBuilder prepareGet
      = client().prepareGet(restApiUrl + "/" + path);
    return runRequest(prepareGet, null, params);

  }

  @Override
  public String post(List<String> path, String body) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1} body: {2}",
        new Object[]{"POST", composeUrl(path), body});
    }
    BoundRequestBuilder preparePost
      = client().preparePost(composeUrl(path));
    return runRequest(preparePost, body, null);
  }

  @Override
  public String post(List<String> path, String body, Map<String, String> params) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1} body: {2} params: {3}",
        new Object[]{"POST", composeUrl(path), body, params});
    }
    BoundRequestBuilder preparePost
      = client().preparePost(composeUrl(path));
    return runRequest(preparePost, body, params);
  }

  @Override
  public String put(List<String> path, String body, Map<String, String> params) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1} body: {2} params: {3}",
        new Object[]{"PUT", composeUrl(path), body, params});
    }
    BoundRequestBuilder preparePut
      = client().preparePut(composeUrl(path));
    return runRequest(preparePut, body, params);
  }

  @Override
  public String patch(List<String> path, String body, Map<String, String> params) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1} body: {2} params: {3}", new Object[]{"PATCH", composeUrl(path), body, params});
    }
    BoundRequestBuilder request
      = client().preparePatch(composeUrl(path));
    return runRequest(request, body, params);
  }

  @Override
  public String delete(List<String> path, Map<String, String> params) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1}",
        new Object[]{"DELETE", composeUrl(path)});
    }
    BoundRequestBuilder prepareDelete
      = client().prepareDelete(composeUrl(path));
    return runRequest(prepareDelete, null, params);
  }

  @Override
  public String postBinary(List<String> path, InputStream dataStream) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1}",
        new Object[]{"POST-binary", composeUrl(path)});
    }
    BoundRequestBuilder preparePost
      = client().preparePost(composeUrl(path));
    preparePost.setBody(dataStream);
    return runRequest(preparePost, null, null);
  }

  @Override
  public void putBinary(List<String> path, InputStream dataStream, Map<String, String> params) {
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "operation: {0} path: {1}",
        new Object[]{"POST-binary", composeUrl(path)});
    }
    BoundRequestBuilder preparePut
      = client().preparePut(composeUrl(path));
    preparePut.setBody(dataStream);
    runBasicRequest(preparePut, null, params);
  }

  @Override
  public InputStream getBinary(List<String> path, Map<String, String> params) {

    BoundRequestBuilder prepareGet
      = client().prepareGet(composeUrl(path));

    prepareGet.addHeader(HEADER_ACCEPT, CONTENT_TYPE_OCTECT_STREAM);
    for (String key : params.keySet()) {
      prepareGet.addQueryParam(key, params.get(key));
    }

    addToken(prepareGet);

    try {
      if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
        HttpClientHelper.debugRequest(prepareGet.build(), LOG, HiroClient.DEBUG_REST_LEVEL);
      }
      final Response r = prepareGet.execute().get(timeout, TimeUnit.MILLISECONDS);
      if (tokenProvider.checkTokenRenewal(r)) {
        tokenProvider.renewToken();
      }
      return checkResponse(prepareGet.execute().get(timeout, TimeUnit.MILLISECONDS)).getResponseBodyAsStream();
    } catch (Throwable t) {
      return Throwables.unchecked(t);
    }
  }

  private final String composeUrl(List<String> parts) {
    StringBuilder url = new StringBuilder(restApiUrl);
    if (!apiPath.isEmpty()) {
      url.append("/");
      url.append(apiPath);
    }
    for (String part : notEmpty(parts, "parts")) {
      try {
        url.append("/");
        url.append(URLEncoder.encode(part, "UTF-8"));
      } catch (Throwable t) {
        return Throwables.unchecked(t);
      }
    }
    return url.toString();
  }

  public String getRedirectLocation(List<String> path, Map<String, String> params) {
    BoundRequestBuilder prepareGet
      = client().prepareGet(composeUrl(path)).setFollowRedirect(false);
    Response response = runBasicRequest(prepareGet, null, params);
    if (REDIRECT_CODES.contains(response.getStatusCode())) {
      if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
        LOG.log(HiroClient.DEBUG_REST_LEVEL, "Found redirect with code={0} location={1}",
          new Object[]{response.getStatusCode(), response.getHeader("Location")});
      }
      return (String) response.getHeader("Location");
    } else {
      throw new RuntimeException("Got unexpected HTTP code (" + Integer.toString(response.getStatusCode())
        + ") from expected redirect request: " + prepareGet.toString());
    }
  }

  public InputStream getBinaryFromStaticLocation(String url) {
    if (url != null) {
      try {
        URL resourceUrl = new URL(url);
        final List bpaths = HiroCollections.newList(resourceUrl.getPath().split("\\/"));
        bpaths.remove(0); // remove /
        URL baseUrl = new URL(resourceUrl.getProtocol(), resourceUrl.getHost(),
          resourceUrl.getPort(), "");
        AuthenticatedRestClient binClient = new AuthenticatedRestClient(baseUrl.toString(),
          new FixedTokenProvider("NO_TOKEN_REQUIRED"), trustAllCerts, debugRestLevel, timeout);
        return binClient.getBinary(bpaths, HiroCollections.newMap());
      } catch (MalformedURLException ex) {
        throw new RuntimeException("got illegal redirect URL for avatar: " + url);
      }
    } else {
      throw new RuntimeException("failed to get redirect URL of avatar");
    }

  }

  private String runRequest(BoundRequestBuilder builder, String json, Map<String, String> parameters) {
    int tries = 0;
    boolean retryToken = true;
    while (true) {
      ++tries;
      try {
        final Response resp = runBasicRequest(builder, json, parameters);
        return resp.getResponseBody(DEFAULT_ENCODING);
      } catch (HiroException ex) {
        if (tokenProvider.checkTokenRenewal(ex.getCode()) && retryToken) {
          retryToken = false;
          tokenProvider.renewToken();
          continue;
        } else if (shouldRetry(ex.getCode(), tries)) {
          backoff(tries);
          continue;
        }

        throw (ex);
      } catch (Throwable t) {
        return Throwables.unchecked(t);
      }
    }
  }

  private void runAsyncRequest(BoundRequestBuilder builder, Map<String, String> parameters, final Listener<Map> listener, boolean retryToken) {
    try {
      addDefaultHeaders(builder, parameters);
      addParameters(builder, parameters);

      if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
        HttpClientHelper.debugRequest(builder.build(), LOG, HiroClient.DEBUG_REST_LEVEL);
      }

      ListenableFuture<Response> execute = builder.execute(new AsyncHandler<Response>() {
        private final Response.ResponseBuilder builder = new Response.ResponseBuilder();
        private final ObjectMapper mapper = new ObjectMapper();

        SurfingConfiguration config = JsonSurferJackson.INSTANCE.configBuilder()
          .bind("$.items[*]", new JsonPathListener() {
            @Override
            public void onValue(Object value, ParsingContext context) {
              listener.process(mapper.convertValue(value, Map.class));
            }
          })
          .build();

        private final NonBlockingParser parser = JsonSurferJackson.INSTANCE.createNonBlockingParser(config);

        @Override
        public void onThrowable(Throwable thrwbl) {
          listener.onException(thrwbl);
        }

        @Override
        public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart hrbp) throws Exception {
          parser.feed(hrbp.getBodyPartBytes(), 0, hrbp.getBodyPartBytes().length);

          return AsyncHandler.State.CONTINUE;
        }

        @Override
        public AsyncHandler.State onStatusReceived(HttpResponseStatus hrs) throws Exception {
          builder.accumulate(hrs);
          return AsyncHandler.State.CONTINUE;
        }

        @Override
        public AsyncHandler.State onHeadersReceived(HttpHeaders hrh) throws Exception {
          builder.accumulate(hrh);
          return AsyncHandler.State.CONTINUE;
        }

        @Override
        public Response onCompleted() throws Exception {
          parser.endOfInput();
          listener.onFinish();
          return builder.build();
        }
      });
      final Response r = execute.get(timeout, TimeUnit.MILLISECONDS);
      if (tokenProvider.checkTokenRenewal(r) && retryToken) {
        tokenProvider.renewToken();
        runAsyncRequest(builder, parameters, listener, false);
      }
      checkResponse(r);
    } catch (Throwable t) {
      Throwables.unchecked(t);
    }
  }

  private org.asynchttpclient.Response runBasicRequest(BoundRequestBuilder builder, String json, Map<String, String> parameters) {
    try {
      addDefaultHeaders(builder, parameters);
      addParameters(builder, parameters);
      addBody(builder, json);

      if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
        HttpClientHelper.debugRequest(builder.build(), LOG, HiroClient.DEBUG_REST_LEVEL);
      }

      // having a timeout is good practice, even if it is one week
      Response response = builder.execute().get(timeout, TimeUnit.MILLISECONDS);
      if (LOG.isLoggable(HiroClient.DEBUG_REST_LEVEL)) {
        HttpClientHelper.debugResponse(response, LOG, HiroClient.DEBUG_REST_LEVEL);
      }
      return checkResponse(response);
    } catch (Throwable t) {
      return Throwables.unchecked(t);
    }
  }

  private void addBody(BoundRequestBuilder builder, String json) {
    if (!isEmpty(json)) {
      builder.setHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_DEFAULT);
      builder.setBody(json);
    }
  }

  private void addParameters(BoundRequestBuilder builder, Map<String, String> parameters) {
    if (parameters != null && !parameters.isEmpty()) {
      // NOTE entryset is very bad
      for (String key : parameters.keySet()) {
        builder.addQueryParam(key, parameters.get(key));
      }
    }
  }

  private static Response checkResponse(Response response) throws Exception {

    final int status = response.getStatusCode();

    if (status >= 200 && status <= 399) {
      return response;
    } else {
      throw new HiroException(tryUnwrap(response.getResponseBody(), response.getStatusText()), status, isEmpty(response.getResponseBody())?null:JSONValue.parse(response.getResponseBody()));
    }
  }

  public final AsyncHttpClient client() {
    return client;
  }

  public static String tryUnwrap(String body, String statusText) {
    if (isEmpty(body)) {
      return statusText;
    }

    Object p = JSONValue.parse(body);

    if (!(p instanceof Map)) {
      return statusText;
    }

    Map m = (Map) p;

    if (!(m.get("error") instanceof Map)) {
      // e.g. required for KI validation response
      if (m.get("error") instanceof String) {
        return (String) m.get("error");
      } else {
        return statusText;
      }
    }

    Map error = (Map) m.get("error");

    if (!(error.get("message") instanceof String)) {
      return statusText;
    }

    return (String) error.get("message");
  }

  private static boolean isEmpty(String str) {
    return str == null || str.isEmpty();
  }

  private boolean shouldRetry(int code, int tries) {
    // 501, 502, ... 888
    return tries < MAX_TRIES && (code > 500 || code == 0);
  }

  private void backoff(int tries) {
    try {
      Thread.sleep(tries * 50);
    } catch (Throwable e) {
      Throwables.unchecked(e);
    }
  }

  private void addDefaultHeaders(BoundRequestBuilder builder, Map<String, String> parameters) {
    if (parameters != null && parameters.containsKey(HEADER_ON_BEHALF_TOKEN)) {
      builder.setHeader(HEADER_ON_BEHALF_TOKEN, parameters.remove(HEADER_ON_BEHALF_TOKEN));
    }
    if (parameters != null && parameters.containsKey(HEADER_CONTENT_TYPE)) {
      builder.setHeader(HEADER_CONTENT_TYPE, parameters.remove(HEADER_CONTENT_TYPE));
    }
    if (parameters != null && parameters.containsKey(HEADER_IMPORT)) {
      builder.setHeader(HEADER_IMPORT, parameters.remove(HEADER_IMPORT));
    }
    addToken(builder);
    builder.setHeader(HEADER_ACCEPT, CONTENT_TYPE_DEFAULT);
  }

  private void addToken(BoundRequestBuilder builder) {
    builder.setHeader("Authorization", "Bearer " + tokenProvider.getToken());
  }

  @Override
  public void close() throws IOException {
    client.close();
    tokenProvider.close();
  }

}

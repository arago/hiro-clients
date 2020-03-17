package co.arago.hiro.action.client.rest;

import co.arago.hiro.action.client.api.HiroActionClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClient;

import static co.arago.hiro.client.util.Helper.*;

public class DefaultHiroActionClient implements HiroActionClient {

  private final AuthenticatedRestClient restClient;

  public DefaultHiroActionClient(String restApiUrl, TokenProvider tokenProvider, boolean trustAllCerts, Level debugLevel) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, 0, null);
  }

  public DefaultHiroActionClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client, boolean trustAllCerts, Level debugLevel, int timeout, String apiVersion) {
    String apiPath = "";
    if (apiVersion != null && !apiVersion.isEmpty()) {
      apiPath = StringUtils.join(HiroCollections.newList(HiroActionClient.PATH[0], apiVersion, HiroActionClient.PATH[2]), "/");
    } else {
      apiPath = StringUtils.join(HiroCollections.newList(HiroActionClient.PATH[0], HiroActionClient.PATH[1], HiroActionClient.PATH[2]), "/");
    }

    restClient = new AuthenticatedRestClient(restApiUrl, tokenProvider, client, trustAllCerts, debugLevel, timeout, apiPath);
  }

  @Override
  public void close() throws IOException {
    restClient.close();
  }

  @Override
  public Map listCapabilities() {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACTIONCAPABILITIES);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.unwrapJsonMap(result);
  }

  @Override
  public Map getAppApplicabilities() {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACTIONAPPLICABILITIES);
    final String result = restClient.get(paths, Collections.EMPTY_MAP);
    return Helper.unwrapJsonMap(result);
  }

  private Map updateFromPath(String id, Map attributes, String path) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(path, "path"));
    paths.add(notNull(id, "id"));
    final String result = restClient.put(paths, Helper.composeJson(attributes), HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  private Map deleteFromPath(String id, String path) {
    return deleteFromPath(id, path, HiroCollections.newMap());
  }

  private Map deleteFromPath(String id, String path, Map params) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(path, "path"));
    paths.add(notNull(id, "id"));
    final String result = restClient.delete(paths, params);
    return Helper.parseJsonBody(result);
  }

  private Map getFromPath(String id, String path) {
    return getFromPath(id, path, HiroCollections.newMap());
  }

  private Map getFromPath(String id, String path, Map<String, String> requestParameters) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(path, "path"));
    paths.add(notNull(id, "id"));
    final String result = restClient.get(paths, requestParameters);
    return Helper.parseJsonBody(result);
  }
}

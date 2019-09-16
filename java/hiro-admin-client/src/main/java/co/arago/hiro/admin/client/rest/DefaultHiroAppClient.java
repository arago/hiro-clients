package co.arago.hiro.admin.client.rest;

import co.arago.hiro.admin.client.api.HiroAppClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.asynchttpclient.AsyncHttpClient;

import static co.arago.hiro.client.util.Helper.notNull;

public class DefaultHiroAppClient implements HiroAppClient {

  private final AuthenticatedRestClient restClient;

  public DefaultHiroAppClient(String restApiUrl, TokenProvider tokenProvider, boolean trustAllCerts, Level debugLevel) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel);
  }

  public DefaultHiroAppClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client, boolean trustAllCerts, Level debugLevel) {
    restClient = new AuthenticatedRestClient(restApiUrl, tokenProvider, client, trustAllCerts, debugLevel, 0);
  }

  @Override
  public void close() throws IOException {
    restClient.close();
  }

  @Override
  public Map createApplication(ApplicationType type, Map<String, String> attributes) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(notNull(type, "type").toString());
    String result = restClient.post(paths, Helper.composeJson(attributes));
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map activate(String ogitId, Map<String, String> attributes) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(notNull(ogitId, "ogitId").toString());
    String result = restClient.patch(paths, Helper.composeJson(attributes), attributes);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map update(String ogitId, Map<String, String> attributes) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(notNull(ogitId, "ogitId").toString());
    String result = restClient.put(paths, Helper.composeJson(attributes), attributes);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map get(String ogitId) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(notNull(ogitId, "ogitId").toString());
    String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map delete(String ogitId) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(notNull(ogitId, "ogitId").toString());
    String result = restClient.delete(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public void install(String applicationId) {
    final List paths = HiroCollections.newList(PATH);
    paths.add("install");
    paths.add(applicationId);
    final Map params = HiroCollections.newMap();
    restClient.post(paths, Helper.composeJson(params));
  }

  @Override
  public void uninstall(String applicationId) {
    final List paths = HiroCollections.newList(PATH);
    paths.add("uninstall");
    paths.add(applicationId);
    final Map params = HiroCollections.newMap();
    restClient.post(paths, Helper.composeJson(params));
  }

  @Override
  public List<Map> listDesktopApps(int limit, int offset) {
    final List paths = HiroCollections.newList(PATH);
    paths.add("desktop");
    final Map params = HiroCollections.newMap();
    params.put("limit", String.valueOf(limit));
    params.put("offset", String.valueOf(offset));
    String result = restClient.get(paths, params);
    return Helper.parseItemList(result);
  }

  @Override
  public List<Map> listAccountDesktopApps(int limit, int offset) {
    final List paths = HiroCollections.newList(PATH);
    paths.add("desktop");
    paths.add("installed");
    final Map params = HiroCollections.newMap();
    params.put("limit", String.valueOf(limit));
    params.put("offset", String.valueOf(offset));
    String result = restClient.get(paths, params);
    return Helper.parseItemList(result);
  }

  @Override
  public void updateAppContent(String ogitId, InputStream is) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(ogitId);
    paths.add("content");
    restClient.putBinary(paths, is);
  }

  @Override
  public InputStream getAppFile(String ogitId, String filename) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(ogitId);
    paths.add("content");
    paths.add(filename);
    return restClient.getBinary(paths, HiroCollections.newMap());
  }

}

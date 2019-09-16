package co.arago.hiro.action.client.rest;

import co.arago.hiro.action.client.api.HiroActionClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClient;

import static co.arago.hiro.client.api.RestClient.*;
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
  public Map createActionHandler(Map attributes) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(URL_PATH_ACTIONHANDLER, "path"));
    final Map params = HiroCollections.newMap();
    if (attributes.containsKey(HEADER_IMPORT)) {
      params.put(HEADER_IMPORT, attributes.remove(HEADER_IMPORT));
    }
    final String result = restClient.post(paths, Helper.composeJson(attributes), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map getActionHandler(String id) {
    return getFromPath(id, URL_PATH_ACTIONHANDLER);
  }

  @Override
  public Map updateActionHandler(String id, Map attributes) {
    return updateFromPath(id, attributes, URL_PATH_ACTIONHANDLER);
  }

  @Override
  public Map deleteActionHandler(String id) {
    return deleteFromPath(id, URL_PATH_ACTIONHANDLER);
  }

  @Override
  public Map createCapability(Map attributes) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(URL_PATH_ACTIONCAPABILITY, "path"));
    final Map params = HiroCollections.newMap();
    if (attributes.containsKey(HEADER_IMPORT)) {
      params.put(HEADER_IMPORT, attributes.remove(HEADER_IMPORT));
    }
    final String result = restClient.post(paths, Helper.composeJson(attributes), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map getCapability(String id) {
    return getFromPath(id, URL_PATH_ACTIONCAPABILITY);
  }

  @Override
  public Map updateCapability(String id, Map attributes) {
    return updateFromPath(id, attributes, URL_PATH_ACTIONCAPABILITY);
  }

  @Override
  public Map deleteCapability(String id) {
    return deleteFromPath(id, URL_PATH_ACTIONCAPABILITY);
  }

  @Override
  public Map createApplicability(Map attributes) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(URL_PATH_ACTIONAPPLICABILITY, "path"));
    final Map params = HiroCollections.newMap();
    if (attributes.containsKey(HEADER_IMPORT)) {
      params.put(HEADER_IMPORT, attributes.remove(HEADER_IMPORT));
    }
    final String result = restClient.post(paths, Helper.composeJson(attributes), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map getApplicability(String id) {
    return getFromPath(id, URL_PATH_ACTIONAPPLICABILITY);
  }

  @Override
  public Map updateApplicability(String id, Map attributes) {
    return updateFromPath(id, attributes, URL_PATH_ACTIONAPPLICABILITY);
  }

  @Override
  public Map deleteApplicability(String id) {
    return deleteFromPath(id, URL_PATH_ACTIONAPPLICABILITY);
  }

  @Override
  public List<Map> listCapabilities() {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACTIONCAPABILITIES);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> getAppCapabilities(String appConfigId) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_APP);
    paths.add(notNull(appConfigId, "appConfigid"));
    paths.add(URL_PATH_ACTIONCAPABILITIES);
    final String result = restClient.get(paths, Collections.EMPTY_MAP);
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> getAppApplicabilities(String appConfigId) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_APP);
    paths.add(notNull(appConfigId, "appConfigid"));
    paths.add(URL_PATH_ACTIONAPPLICABILITIES);
    final String result = restClient.get(paths, Collections.EMPTY_MAP);
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> getAppActionHandlers(String appConfigId) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_APP);
    paths.add(notNull(appConfigId, "appConfigid"));
    paths.add(URL_PATH_ACTIONHANDLERS);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> setAppActionHandlers(String appConfigId, Collection<String> actionHandlers) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(URL_PATH_APP, "path"));
    paths.add(notNull(appConfigId, "appConfigid"));
    paths.add(URL_PATH_ACTIONHANDLERS);
    final Map attributes = HiroCollections.newMap();
    attributes.put("actionHandlers", actionHandlers);
    final String result = restClient.post(paths, Helper.composeJson(attributes), HiroCollections.newMap());
    return Helper.parseItemListOfMaps(result);
  }

  @Override
  public List<Map> setActionHandlerApplicabilities(String actionHandlerId, Collection<String> applicabilities) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(URL_PATH_ACTIONHANDLER, "path"));
    paths.add(notNull(actionHandlerId, "actionHandlerId"));
    paths.add(URL_PATH_ACTIONAPPLICABILITIES);
    final Map attributes = HiroCollections.newMap();
    attributes.put("applicabilities", applicabilities);
    final String result = restClient.post(paths, Helper.composeJson(attributes), HiroCollections.newMap());
    return Helper.parseItemListOfMaps(result);
  }

  @Override
  public List<Map> setApplicabilityCapabilities(String applicability, Collection<String> capabilities) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(URL_PATH_ACTIONAPPLICABILITY, "path"));
    paths.add(notNull(applicability, "applicabilityId"));
    paths.add(URL_PATH_ACTIONCAPABILITIES);
    final Map attributes = HiroCollections.newMap();
    attributes.put("capabilities", capabilities);
    final String result = restClient.post(paths, Helper.composeJson(attributes), HiroCollections.newMap());
    return Helper.parseItemListOfMaps(result);
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

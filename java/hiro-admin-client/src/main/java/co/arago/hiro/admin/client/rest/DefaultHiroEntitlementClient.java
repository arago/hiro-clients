package co.arago.hiro.admin.client.rest;

import co.arago.hiro.admin.client.api.HiroEntitlementClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.asynchttpclient.AsyncHttpClient;

import org.apache.commons.lang.StringUtils;

public class DefaultHiroEntitlementClient implements HiroEntitlementClient {

  private final AuthenticatedRestClient restClient;

  public DefaultHiroEntitlementClient(String restApiUrl, TokenProvider tokenProvider,
    boolean trustAllCerts, Level debugLevel) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, 0, null);
  }

  public DefaultHiroEntitlementClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client,
    boolean trustAllCerts, Level debugLevel, int timeout, String apiVersion) {
    String apiPath = "";
    if (apiVersion != null && !apiVersion.isEmpty()) {
      apiPath = StringUtils.join(HiroCollections.newList(HiroEntitlementClient.PATH[0], apiVersion, HiroEntitlementClient.PATH[2]), "/");
    } else {
      apiPath = StringUtils.join(HiroCollections.newList(HiroEntitlementClient.PATH[0], HiroEntitlementClient.PATH[1], HiroEntitlementClient.PATH[2]), "/");
    }
    restClient = new AuthenticatedRestClient(restApiUrl, tokenProvider,
      client, trustAllCerts, debugLevel, timeout, apiPath);
  }

  @Override
  public void close() throws IOException {
    restClient.close();
  }

  @Override
  public Map checkEntitlement(String token, List<Map> items) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ENTITLEMENT);
    final Map params = HiroCollections.newMap();
    params.put("token", token);
    params.put("items", items);
    final String result = restClient.post(paths, Helper.composeJson(params));
    return Helper.parseJsonBody(result);
  }

}

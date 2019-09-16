package co.arago.hiro.admin.client.rest;

import co.arago.hiro.admin.client.api.HiroIamClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.asynchttpclient.AsyncHttpClient;

import static co.arago.hiro.client.api.OGITConstants.Attributes.OGIT_NAME;
import static co.arago.hiro.client.util.Helper.notNull;

/**
 *
 */
public class DefaultHiroIamClient implements HiroIamClient {

  private final AuthenticatedRestClient restClient;

  public DefaultHiroIamClient(String restApiUrl, TokenProvider tokenProvider,
    boolean trustAllCerts, Level debugLevel) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, 0);
  }

  public DefaultHiroIamClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client,
    boolean trustAllCerts, Level debugLevel, int timeout) {
    restClient = new AuthenticatedRestClient(restApiUrl, tokenProvider,
      client, trustAllCerts, debugLevel, timeout);
  }

  @Override
  public void close() throws IOException {
    restClient.close();
  }

  @Override
  public Map createIdentity(IdentityType type, String name, boolean active, Map<String, String> attributes) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(notNull(type, "type").toString());
    attributes.put(OGIT_NAME, name);
    attributes.put(QUERY_PARAM_ACTIVE, String.valueOf(active));
    final String result = restClient.post(paths, Helper.composeJson(attributes));
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map deactivateIdentity(IdentityType type, String id) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(notNull(type, "type").toString());
    paths.add(notNull(id, "id"));
    final String result = restClient.delete(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public List<Map> accountRoles(String id) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.accounts.toString());
    paths.add(notNull(id, "id"));
    paths.add(IdentityType.roles.toString());
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseItemList(result);
  }

  @Override
  public List<Map> meRoles() {
    final List paths = HiroCollections.newList(PATH);
    paths.add(URL_PATH_ME);
    paths.add(IdentityType.roles.toString());
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseItemList(result);
  }

  @Override
  public Map meAccount() {
    final List paths = HiroCollections.newList(PATH);
    paths.add(URL_PATH_ME);
    paths.add("account");
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map mePerson() {
    final List paths = HiroCollections.newList(PATH);
    paths.add(URL_PATH_ME);
    paths.add("person");
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map updateIdentity(IdentityType type, String id, Map<String, String> attributes) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(notNull(type, "type").toString());
    paths.add(notNull(id, "id"));
    final String result = restClient.post(paths, Helper.composeJson(attributes));
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map accountPerson(String id) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.accounts.toString());
    paths.add(notNull(id, "id"));
    paths.add("person");
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public List<Map> listRoles(int limit, int offset, String name) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.roles.toString());
    final Map params = HiroCollections.newMap();
    params.put("limit", String.valueOf(limit));
    params.put("offset", String.valueOf(offset));
    params.put("name", name);
    final String result = restClient.get(paths, params);
    return Helper.parseItemList(result);
  }

  @Override
  public List<Map> listAccounts(int limit, int offset, String name) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.accounts.toString());
    final Map params = HiroCollections.newMap();
    params.put("limit", String.valueOf(limit));
    params.put("offset", String.valueOf(offset));
    params.put("name", name);
    final String result = restClient.get(paths, params);
    return Helper.parseItemList(result);
  }

  @Override
  public Map modifyRoleAccounts(String role, boolean remove, String... accounts) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.roles.toString());
    paths.add(notNull(role, "role"));
    paths.add(IdentityType.accounts.toString());
    final Map params = HiroCollections.newMap();
    params.put("accounts", String.join(",", Arrays.asList(accounts)));
    params.put("remove", String.valueOf(remove));
    final String result = restClient.post(paths, Helper.composeJson(params), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public List<Map> roleAccounts(String id) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.roles.toString());
    paths.add(notNull(id, "id"));
    paths.add(IdentityType.accounts.toString());
    final String result = restClient.get(paths, Helper.composeJson(HiroCollections.newMap()), HiroCollections.newMap());
    return Helper.parseItemList(result);
  }

  @Override
  public Map modifyAccountRoles(String account, boolean remove, String... roles) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.accounts.toString());
    paths.add(notNull(account, "account"));
    paths.add(IdentityType.roles.toString());
    final Map params = HiroCollections.newMap();
    params.put("roles", String.join(",", Arrays.asList(roles)));
    params.put("remove", String.valueOf(remove));
    final String result = restClient.post(paths, Helper.composeJson(params));
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map updatePassword(String id, String password) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.accounts.toString());
    paths.add(id);
    paths.add("password");
    final Map params = HiroCollections.newMap();
    params.put("password", password);
    final String result = restClient.put(paths, Helper.composeJson(params), null);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map mePassword(String oldPassword, String newPassword) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(URL_PATH_ME);
    paths.add("password");
    final Map params = HiroCollections.newMap();
    params.put("oldPassword", oldPassword);
    params.put("newPassword", newPassword);
    final String result = restClient.put(paths, Helper.composeJson(params), null);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map activateAccount(String id) {
    final List paths = HiroCollections.newList(PATH);
    paths.add(IdentityType.accounts.toString());
    paths.add(id);
    final Map params = HiroCollections.newMap();
    final String result = restClient.patch(paths, Helper.composeJson(params), params);
    return Helper.parseJsonBody(result);
  }

}

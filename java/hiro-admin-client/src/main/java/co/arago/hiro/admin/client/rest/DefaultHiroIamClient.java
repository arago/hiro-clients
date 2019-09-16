package co.arago.hiro.admin.client.rest;

import co.arago.hiro.admin.client.api.HiroIamClient;
import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.rest.AuthenticatedRestClient;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.HiroCollections;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.asynchttpclient.AsyncHttpClient;

import static co.arago.hiro.client.api.OGITConstants.Attributes.*;
import static co.arago.hiro.client.api.RestClient.*;
import static co.arago.hiro.client.util.Helper.*;

public class DefaultHiroIamClient implements HiroIamClient {

  private final AuthenticatedRestClient restClient;

  public DefaultHiroIamClient(String restApiUrl, TokenProvider tokenProvider,
    boolean trustAllCerts, Level debugLevel) {
    this(restApiUrl, tokenProvider, null, trustAllCerts, debugLevel, 0, null);
  }

  public DefaultHiroIamClient(String restApiUrl, TokenProvider tokenProvider, AsyncHttpClient client,
    boolean trustAllCerts, Level debugLevel, int timeout, String apiVersion) {
    String apiPath = "";
    if (apiVersion != null && !apiVersion.isEmpty()) {
      apiPath = StringUtils.join(HiroCollections.newList(HiroIamClient.PATH[0], apiVersion, HiroIamClient.PATH[2]), "/");
    } else {
      apiPath = StringUtils.join(HiroCollections.newList(HiroIamClient.PATH[0], HiroIamClient.PATH[1], HiroIamClient.PATH[2]), "/");
    }
    restClient = new AuthenticatedRestClient(restApiUrl, tokenProvider,
      client, trustAllCerts, debugLevel, timeout, apiPath);
  }

  @Override
  public void close() throws IOException {
    restClient.close();
  }

  @Override
  public Map createAccount(String name, boolean active, Map<String, String> attributes) {
    attributes.put(OGIT_NAME, notNull(name, "name"));
    attributes.put(OGIT_STATUS, notNull(active, "active").equals(true) ? "active" : "inactive");
    final List paths = HiroCollections.newList();
    paths.add(notNull(URL_PATH_ACCOUNTS, "path"));
    final Map params = HiroCollections.newMap();
    if (attributes.containsKey(HEADER_IMPORT)) {
      params.put(HEADER_IMPORT, attributes.remove(HEADER_IMPORT));
    }
    final String result = restClient.post(paths, Helper.composeJson(attributes), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map deactivateAccount(String id, String reason) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(id);
    paths.add(URL_PATH_DEACTIVATE);
    final Map params = HiroCollections.newMap();
    params.put(AUTH_ACCOUNT_STATUS_REASON, notNull(reason, "reason"));
    final String result = restClient.patch(paths, Helper.composeJson(params), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public void deleteAccount(String id) {
    deleteFromPath(id, URL_PATH_ACCOUNTS);
  }

  @Override
  public InputStream getAvatar(String accountId) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(accountId);
    paths.add(URL_PATH_AVATAR);
    return restClient.getBinaryFromStaticLocation(restClient.getRedirectLocation(paths, HiroCollections.newMap()));
  }

  @Override
  public void setAvatar(String accountId, InputStream is, String contentType) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(accountId);
    paths.add(URL_PATH_AVATAR);
    restClient.putBinary(paths, is, HiroCollections.newMap(HEADER_CONTENT_TYPE, contentType));
  }

  @Override
  public InputStream getOrgAvatar(String orgId) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORGANIZATION);
    paths.add(orgId);
    paths.add(URL_PATH_AVATAR);
    return restClient.getBinaryFromStaticLocation(restClient.getRedirectLocation(paths, HiroCollections.newMap()));
  }

  @Override
  public void setOrgAvatar(String orgId, InputStream is, String contentType) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORGANIZATION);
    paths.add(orgId);
    paths.add(URL_PATH_AVATAR);
    restClient.putBinary(paths, is, HiroCollections.newMap(HEADER_CONTENT_TYPE, contentType));
  }

  @Override
  public Map updateAccount(String id, Map<String, String> attributes) {
    final List paths = HiroCollections.newList();
    final Map params = HiroCollections.newMap();
    paths.add(notNull(URL_PATH_ACCOUNTS, "path"));
    paths.add(notNull(id, "id"));
    final String result = restClient.post(paths, Helper.composeJson(attributes), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map getAccount(String id, Map<String, String> requestParameters) {
    return getFromPath(id, URL_PATH_ACCOUNTS, requestParameters);
  }

  @Override
  public Map updateAccountProfile(String id, Map<String, String> attributes) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(URL_PATH_PROFILE);
    paths.add(id);
    final String result = restClient.post(paths, Helper.composeJson(attributes), HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map getAccountProfile(String id) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(id);
    paths.add(URL_PATH_PROFILE);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public List<Map> getAccountTeams(String id, boolean includeVirtualTeams) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(id);
    paths.add(URL_PATH_TEAMS);
    final Map params = HiroCollections.newMap();
    if (includeVirtualTeams) {
      params.put(QUERY_PARAM_VIRTUAL_TEAMS, "true");
    }
    final String result = restClient.get(paths, params);
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public Map getAccountProfileByAccountId(String id) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(id);
    paths.add(URL_PATH_PROFILE);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public List<Map> listRoles(int limit, int offset, String name) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ROLES);
    final Map params = HiroCollections.newMap();
    params.put("limit", String.valueOf(limit));
    params.put("offset", String.valueOf(offset));
    params.put("name", name);
    final String result = restClient.get(paths, params);
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> listAccounts(int limit, int offset, String name) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    final Map params = HiroCollections.newMap();
    params.put("limit", String.valueOf(limit));
    params.put("offset", String.valueOf(offset));
    params.put("name", name);
    final String result = restClient.get(paths, params);
    return Helper.parseItemListOfMaps(result);
  }

  @Override
  public Map<String, Map> listAccountProfiles(int limit, int offset) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNT);
    paths.add(URL_PATH_PROFILES);
    final Map params = HiroCollections.newMap();
    params.put("limit", String.valueOf(limit));
    params.put("offset", String.valueOf(offset));
    final String result = restClient.get(paths, params);
    return Helper.unwrapJsonMap(result);
  }

  @Override
  public void updatePassword(String id, String password) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(id);
    paths.add("password");
    final Map params = HiroCollections.newMap();
    params.put("password", password);
    restClient.put(paths, Helper.composeJson(params), null);
  }

  @Override
  public Map activateAccount(String id) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ACCOUNTS);
    paths.add(id);
    paths.add(URL_PATH_ACTIVATE);
    final Map params = HiroCollections.newMap();
    final String result = restClient.patch(paths, Helper.composeJson(params), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map createDataSet(String name, String description, String vertexRule, String edgeRule, Map attributes) {
    attributes.put(OGIT_NAME, notNull(name, "name"));
    attributes.put(OGIT_DESCRIPTION, notNull(description, "description"));
    attributes.put(AUTH_VERTEXRULE, notNull(vertexRule, "vertexRule"));
    attributes.put(AUTH_EDGERULE, notNull(edgeRule, "edgeRule"));
    return createFromPath(attributes, URL_PATH_DATASET);
  }

  @Override
  public Map updateDataSet(String id, Map attributes) {
    return updateFromPath(id, attributes, URL_PATH_DATASET);
  }

  @Override
  public Map getDataSet(String id) {
    return getFromPath(id, URL_PATH_DATASET);
  }

  @Override
  public Map deleteDataSet(String id) {
    return deleteFromPath(id, URL_PATH_DATASET);
  }

  @Override
  public Map createTeam(String parent, Map attributes) {
    attributes.put("parent", notNull(parent, "parent"));
    return createFromPath(attributes, URL_PATH_TEAM);
  }

  @Override
  public Map updateTeam(String id, Map attributes) {
    return updateFromPath(id, attributes, URL_PATH_TEAM);
  }

  @Override
  public Map getTeam(String id) {
    return getFromPath(id, URL_PATH_TEAM);
  }

  @Override
  public Map deleteTeam(String id) {
    return deleteFromPath(id, URL_PATH_TEAM);
  }

  @Override
  public Map createRole(String name, String description, String vertexRule, String edgeRule, Map attributes) {
    attributes.put(OGIT_NAME, notNull(name, "name"));
    attributes.put(OGIT_DESCRIPTION, notNull(description, "description"));
    attributes.put(AUTH_VERTEXRULE, notNull(vertexRule, "vertexRule"));
    attributes.put(AUTH_EDGERULE, notNull(edgeRule, "edgeRule"));
    return createFromPath(attributes, URL_PATH_ROLE);
  }

  @Override
  public Map updateRole(String id, Map attributes) {
    return updateFromPath(id, attributes, URL_PATH_ROLE);
  }

  @Override
  public Map getRole(String id) {
    return getFromPath(id, URL_PATH_ROLE);
  }

  @Override
  public Map deleteRole(String id) {
    return deleteFromPath(id, URL_PATH_ROLE);
  }

  @Override
  public Map createOrganization(Map attributes) {
    return createFromPath(attributes, URL_PATH_ORGANIZATION);
  }

  @Override
  public List<Map> addMembers(String id, String... accounts) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_TEAM);
    paths.add(notNull(id, "id"));
    paths.add(URL_PATH_MEMBERS);
    paths.add("add");
    final Map params = HiroCollections.newMap();
    params.put("accounts", String.join(",", Arrays.asList(accounts)));
    final String result = restClient.post(paths, Helper.composeJson(params));
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> removeMembers(String id, String... accounts) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_TEAM);
    paths.add(notNull(id, "id"));
    paths.add(URL_PATH_MEMBERS);
    paths.add("remove");
    final Map params = HiroCollections.newMap();
    params.put("accounts", String.join(",", Arrays.asList(accounts)));
    final String result = restClient.post(paths, Helper.composeJson(params));
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> getTeamMembers(String id, Map<String, String> requestParameters) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_TEAM);
    paths.add(notNull(id, "id"));
    paths.add(URL_PATH_MEMBERS);
    final String result = restClient.get(paths, Helper.composeJson(requestParameters), requestParameters);
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> getOrganizationMembers(String id, Map<String, String> requestParameters) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORGANIZATION);
    paths.add(notNull(id, "id"));
    paths.add(URL_PATH_MEMBERS);
    final String result = restClient.get(paths, Helper.composeJson(requestParameters), requestParameters);
    return Helper.unwrapJsonArray(result);
  }

  @Override

  public List<Map> organizationTeams(String organization, boolean includeVirtualTeams) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORGANIZATION);
    paths.add(notNull(organization, "organization"));
    paths.add("teams");
    final Map params = HiroCollections.newMap();
    if (includeVirtualTeams) {
      params.put(QUERY_PARAM_VIRTUAL_TEAMS, "true");
    }
    final String result = restClient.get(paths, params);
    return Helper.parseItemListOfMaps(result);
  }

  @Override
  public Map createRoleAssignment(String roleId, String teamId, String dataSetId, Map attributes) {
    attributes.put("roleId", notNull(roleId, "roleId"));
    attributes.put("teamId", notNull(teamId, "teamId"));
    attributes.put("dataSetId", notNull(dataSetId, "dataSetId"));
    return createFromPath(attributes, URL_PATH_ROLE_ASSIGNMENT);
  }

  @Override
  public Map getRoleAssignment(String id) {
    return getFromPath(id, URL_PATH_ROLE_ASSIGNMENT);
  }

  @Override
  public Map deleteRoleAssignment(String id) {
    return deleteFromPath(id, URL_PATH_ROLE_ASSIGNMENT);
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

  private Map createFromPath(Map attributes, String path) {
    final List paths = HiroCollections.newList();
    paths.add(notNull(path, "path"));
    final String result = restClient.post(paths, Helper.composeJson(attributes));
    return Helper.parseJsonBody(result);
  }

  private Map updateFromPath(String id, Map attributes, String path) {
    final List paths = HiroCollections.newList();
    final Map params = HiroCollections.newMap();
    paths.add(notNull(path, "path"));
    paths.add(notNull(id, "id"));
    final String result = restClient.put(paths, Helper.composeJson(attributes), params);
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map createDomain(String name, String organization) {
    final Map attributes = HiroCollections.newMap();
    attributes.put("name", notNull(name, "name"));
    attributes.put("organization", notNull(organization, "organization"));
    return createFromPath(attributes, URL_PATH_ORG_DOMAIN);
  }

  @Override
  public Map getDomain(String id) {
    return getFromPath(id, URL_PATH_ORG_DOMAIN);
  }

  @Override
  public Map deleteDomain(String id) {
    return deleteFromPath(id, URL_PATH_ORG_DOMAIN);
  }

  @Override
  public List<Map> organizationDomains(String organization) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORGANIZATION);
    paths.add(organization);
    paths.add(URL_PATH_ORG_DOMAINS);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseItemListOfMaps(result);
  }

  @Override
  public List<Map> organizationRoleAssignments(String organization, Map<String, String> requestParameters) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORGANIZATION);
    paths.add(organization);
    paths.add(URL_PATH_ROLE_ASSIGNMENTS);
    final String result = restClient.get(paths, requestParameters);
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public Map getDomainOrganization(String name) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORG_DOMAIN);
    paths.add(name);
    paths.add(URL_PATH_ORGANIZATION);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.parseJsonBody(result);
  }

  @Override
  public Map createDataScope(String name, Map attributes) {
    attributes.put(OGIT_NAME, notNull(name, "name"));
    return createFromPath(attributes, URL_PATH_ORG_SCOPE);
  }

  @Override
  public Map updateDataScope(String id, Map attributes) {
    return updateFromPath(id, attributes, URL_PATH_ORG_SCOPE);
  }

  @Override
  public Map getDataScope(String id) {
    return getFromPath(id, URL_PATH_ORG_SCOPE);
  }

  @Override
  public List<Map> organizationDataSets(String organization) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORGANIZATION);
    paths.add(organization);
    paths.add(URL_PATH_DATA_SETS);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> organizationDataScopes(String organization) {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ORGANIZATION);
    paths.add(organization);
    paths.add(URL_PATH_ORG_SCOPES);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.unwrapJsonArray(result);
  }

  @Override
  public List<Map> listRoles() {
    final List paths = HiroCollections.newList();
    paths.add(URL_PATH_ROLES);
    final String result = restClient.get(paths, HiroCollections.newMap());
    return Helper.unwrapJsonArray(result);
  }

}

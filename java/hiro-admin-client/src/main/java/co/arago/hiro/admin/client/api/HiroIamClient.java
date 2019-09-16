package co.arago.hiro.admin.client.api;

import java.io.Closeable;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface HiroIamClient extends Closeable {

  String[] PATH = new String[]{"api", "6.1", "iam"};
  String QUERY_PARAM_NAME = "name";
  String QUERY_PARAM_ACTIVE = "active";
  String QUERY_PARAM_CONTENT = "content";
  String QUERY_PARAM_VIRTUAL_TEAMS = "include-virtual";
  String URL_PATH_ACCOUNTS = "accounts";
  String URL_PATH_ACCOUNT = "account";
  String URL_PATH_PROFILE = "profile";
  String URL_PATH_PROFILES = "profiles";
  String URL_PATH_AVATAR = "avatar";
  String URL_PATH_PASSWORD = "password";
  String URL_PATH_TEAMS = "teams";
  String URL_PATH_ROLE = "role";
  String URL_PATH_ROLES = "roles";
  String URL_PATH_DATASET = "dataset";
  String URL_PATH_TEAM = "team";
  String URL_PATH_ORGANIZATION = "organization";
  String URL_PATH_MEMBERS = "members";
  String URL_PATH_ROLE_ASSIGNMENT = "roleassignment";
  String URL_PATH_ORG_DOMAIN = "domain";
  String URL_PATH_ORG_DOMAINS = "domains";
  String URL_PATH_ROLE_ASSIGNMENTS = "roleassignments";
  String URL_PATH_ORG_SCOPE = "scope";
  String URL_PATH_ORG_SCOPES = "scopes";
  String URL_PATH_DATA_SETS = "datasets";
  String URL_PATH_ME = "me";
  String URL_PATH_ACTIVATE = "activate";
  String URL_PATH_DEACTIVATE = "deactivate";

  Map createAccount(String name, boolean active, Map<String, String> attributes);

  Map updateAccount(String id, Map<String, String> attributes);

  Map getAccount(String id, Map<String, String> requestParameters);

  Map updateAccountProfile(String id, Map<String, String> attributes);

  Map getAccountProfile(String id);

  Map getAccountProfileByAccountId(String id);

  List<Map> getAccountTeams(String id, boolean includeVirtualTeams);

  default List<Map> getAccountTeams(String id) {
    return getAccountTeams(id, false);
  }

  Map deactivateAccount(String id, String reason);

  Map activateAccount(String id);

  void deleteAccount(String id);

  void setAvatar(String accountId, InputStream is, String contentType);

  InputStream getAvatar(String accountId);

  List<Map> listRoles(int limit, int offset, String name);

  List<Map> listAccounts(int limit, int offset, String name);

  Map<String, Map> listAccountProfiles(int limit, int offset);

  void updatePassword(String id, String password);

  Map createDataSet(String name, String description, String vertexRule, String edgeRule, Map attributes);

  Map updateDataSet(String id, Map attributes);

  Map getDataSet(String id);

  Map deleteDataSet(String id);

  Map createTeam(String parent, Map attributes);

  Map updateTeam(String id, Map attributes);

  Map getTeam(String id);

  Map deleteTeam(String id);

  Map createRole(String name, String description, String vertexRule, String edgeRule, Map attributes);

  Map updateRole(String id, Map attributes);

  Map getRole(String id);

  Map deleteRole(String id);

  Map createOrganization(Map attributes);

  List<Map> addMembers(String id, String... accounts);

  List<Map> removeMembers(String id, String... accounts);

  List<Map> getTeamMembers(String id, Map<String, String> requestParameters);

  List<Map> getOrganizationMembers(String id, Map<String, String> requestParameters);

  List<Map> organizationTeams(String organization, boolean includeVirtualTeams);

  default List<Map> organizationTeams(String organization) {
    return organizationTeams(organization, false);
  }

  List<Map> organizationRoleAssignments(String organization, Map<String, String> requestParameters);

  void setOrgAvatar(String orgId, InputStream is, String contentType);

  InputStream getOrgAvatar(String orgId);

  Map createRoleAssignment(String roleId, String teamId, String dataSetId, Map attributes);

  Map getRoleAssignment(String id);

  Map deleteRoleAssignment(String id);

  Map createDomain(String name, String organization);

  Map getDomain(String id);

  Map deleteDomain(String id);

  List<Map> organizationDomains(String organization);

  List<Map> organizationDataSets(String organization);

  List<Map> organizationDataScopes(String organization);

  List<Map> listRoles();

  Map getDomainOrganization(String name);

  Map createDataScope(String name, Map attributes);

  Map updateDataScope(String id, Map attributes);

  Map getDataScope(String id);
}

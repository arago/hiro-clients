package co.arago.hiro.admin.client.api;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface HiroIamClient extends Closeable {

  enum IdentityType {
    //person, TODO still to be defined
    accounts,
    roles
  }

  String[] PATH = new String[]{"api", "6", "iam"};
  String QUERY_PARAM_NAME = "name";
  String QUERY_PARAM_ACTIVE = "active";
  String URL_PATH_ME = "me";

  Map createIdentity(IdentityType type, String name, boolean active, Map<String, String> attributes);

  Map updateIdentity(IdentityType type, String id, Map<String, String> attributes);

  Map deactivateIdentity(IdentityType type, String id);

  Map activateAccount(String id);

  List<Map> accountRoles(String id);

  Map accountPerson(String id);

  List<Map> meRoles();

  Map meAccount();

  Map mePerson();

  List<Map> listRoles(int limit, int offset, String name);

  List<Map> listAccounts(int limit, int offset, String name);

  Map modifyRoleAccounts(String role, boolean remove, String... accounts);

  List<Map> roleAccounts(String id);

  Map modifyAccountRoles(String account, boolean remove, String... roles);

  Map updatePassword(String id, String password);

  Map mePassword(String oldPassword, String newPassword);
}

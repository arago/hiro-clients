package co.arago.hiro.admin.client.api;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface HiroEntitlementClient extends Closeable {

  String[] PATH = new String[]{"api", "6.1", "authz"};
  String URL_PATH_ENTITLEMENT = "entitlement";

  Map checkEntitlement(String token, List<Map> items);
}

package co.arago.hiro.action.client.api;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface HiroActionClient extends Closeable {

  String[] PATH = new String[]{"api", "action", "1.0"};

  String URL_PATH_ACTIONHANDLER = "handler";
  String URL_PATH_ACTIONHANDLERS = "handlers";
  String URL_PATH_ACTIONCAPABILITY = "capability";
  String URL_PATH_ACTIONCAPABILITIES = "capabilities";
  String URL_PATH_ACTIONAPPLICABILITY = "applicability";
  String URL_PATH_ACTIONAPPLICABILITIES = "applicabilities";
  String URL_PATH_APP = "app";

  Map createActionHandler(Map attributes);

  Map getActionHandler(String id);

  Map updateActionHandler(String id, Map attributes);

  Map deleteActionHandler(String id);

  Map createCapability(Map attributes);

  Map getCapability(String id);

  Map updateCapability(String id, Map attributes);

  Map deleteCapability(String id);

  Map createApplicability(Map attributes);

  Map getApplicability(String id);

  Map updateApplicability(String id, Map attributes);

  Map deleteApplicability(String id);

  Map listCapabilities();

  List<Map> getAppCapabilities(String appConfigId);

  List<Map> getAppApplicabilities(String appConfigId);

  List<Map> getAppActionHandlers(String appConfigId);

  List<Map> setAppActionHandlers(String appConfigId, Collection<String> actionHandlers);

  List<Map> setActionHandlerApplicabilities(String actionHandlerId, Collection<String> applicabilities);

  List<Map> setApplicabilityCapabilities(String applicability, Collection<String> capabilities);
}

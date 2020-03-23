package co.arago.hiro.action.client.api;

import java.io.Closeable;
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

  Map listCapabilities();

  Map getAppApplicabilities();

}

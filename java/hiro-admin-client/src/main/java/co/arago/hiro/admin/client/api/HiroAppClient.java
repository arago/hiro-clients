package co.arago.hiro.admin.client.api;

import java.io.Closeable;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface HiroAppClient extends Closeable {

  enum ApplicationType {
    graph,
    ui,
    desktop
  }

  String[] PATH = new String[]{"api", "6", "app"};

  Map createApplication(ApplicationType type, Map<String, String> attributes);

  Map activate(String ogitId, Map<String, String> attributes);

  Map update(String ogitId, Map<String, String> attributes);

  Map get(String ogitId);

  Map delete(String ogitId);

  void install(String applicationId);

  void uninstall(String applicationId);

  List<Map> listDesktopApps(int limit, int offset);

  List<Map> listAccountDesktopApps(int limit, int offset);

  void updateAppContent(String ogitId, InputStream is);

  InputStream getAppFile(String ogitId, String filename);
}

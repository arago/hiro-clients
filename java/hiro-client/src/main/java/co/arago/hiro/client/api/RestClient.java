package co.arago.hiro.client.api;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import co.arago.hiro.client.util.Listener;

/**
 *
 * @author fotto
 */
public interface RestClient extends Closeable {

  String HEADER_CONTENT_TYPE = "Content-Type";
  String HEADER_ACCEPT = "Accept";
  Charset DEFAULT_ENCODING = Charset.forName("UTF-8");
  String CONTENT_TYPE_DEFAULT = "application/json;charset=UTF-8";
  String CONTENT_TYPE_OCTECT_STREAM = "application/octet-stream";

  void get(List<String> path, Map<String, String> params, Listener<Map> listener);

  String get(List<String> path, Map<String, String> params);

  String get(List<String> path, String body, Map<String, String> params);

  String post(List<String> path, String body);

  String post(List<String> path, String body, Map<String, String> params);

  String put(List<String> path, String body, Map<String, String> params);

  String patch(List<String> path, String body, Map<String, String> params);

  String delete(List<String> path, Map<String, String> params);

  void postBinary(List<String> path, InputStream dataStream);

  void putBinary(List<String> path, InputStream dataStream);

  InputStream getBinary(List<String> path, Map<String, String> params);

}

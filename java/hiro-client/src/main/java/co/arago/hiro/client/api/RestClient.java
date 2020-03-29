package co.arago.hiro.client.api;

import co.arago.hiro.client.util.Listener;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fotto
 */
public interface RestClient extends Closeable {

  final String HEADER_CONTENT_TYPE = "Content-Type";
  final String HEADER_ACCEPT = "Accept";
  final String HEADER_ON_BEHALF_TOKEN = "On-Behalf-Token";
  final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
  final String CONTENT_TYPE_DEFAULT = "application/json;charset=UTF-8";
  final String CONTENT_TYPE_OCTECT_STREAM = "application/octet-stream";
  final String CONTENT_TYPE_IMAGE = "image/png";
  final String HEADER_IMPORT = "import";

  void get(List<String> path, Map<String, String> params, Listener<Map> listener);

  String get(List<String> path, Map<String, String> params);

  String get(List<String> path, String body, Map<String, String> params);

  /**
   * native get
   *
   * path is taken literally. not default prefix will be prepended
   *
   * @param path full path for GET operation
   * @param params URL parameters
   * @return returned body as String
   */
  String get(String path, Map<String, String> params);

  String post(List<String> path, String body);

  String post(List<String> path, String body, Map<String, String> params);

  String put(List<String> path, String body, Map<String, String> params);

  String patch(List<String> path, String body, Map<String, String> params);

  String delete(List<String> path, Map<String, String> params);

  String postBinary(List<String> path, InputStream dataStream);

  void putBinary(List<String> path, InputStream dataStream, Map<String, String> params);

  InputStream getBinary(List<String> path, Map<String, String> params);

}

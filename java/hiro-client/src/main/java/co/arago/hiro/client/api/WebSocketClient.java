package co.arago.hiro.client.api;

import java.util.Map;

public interface WebSocketClient {

  /**
   *
   * @param type
   * @param headers
   * @param body
   * @return the id of this request
   */
  int sendMessage(String type, Map<String, String> headers, Map body);
  
  void sendMessage(String message);

  void close();
}

package co.arago.hiro.cli.util;

import co.arago.hiro.client.util.HiroException;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 *
 */
public class ExceptionHelper {

  public static int exceptionToRc(HiroException ex) {
    int code = ex.getCode();
    if (code >= HTTP_BAD_REQUEST) {
      return code - 390;
    }
    return 1;
  }
}

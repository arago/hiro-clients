package co.arago.hiro.client.util;

import java.io.Serializable;

/**
 * a base exception for HIRO, contains an error code
 */
public class HiroException extends RuntimeException implements Serializable {

  private static final long serialVersionUID = 42L;
  private final int code;
  private final transient Throwable cause;

  public HiroException(final String message, final int code) {
    super(message);

    this.code = code;
    this.cause = null;
  }

  public HiroException(final String message, final int code, final Throwable t) {
    super(message);

    this.code = code;
    this.cause = t;
  }

  /**
   * get the error code
   *
   * @return
   */
  public int getCode() {
    return code;
  }

  @Override
  public synchronized Throwable getCause() {
    return (cause == this ? null : cause);
  }
}

package co.arago.hiro.client.util;

/**
 * Describes the basic building block for the communication of information
 *
 * @param <T> Type of element that the Listener receives
 */
public interface Listener<T> {

  enum ListenerState {

    OK, STOPPED
  }

  /**
   * process a streaming entry
   *
   * @param entry
   * @return status of listener after processing entry
   */
  ListenerState process(T entry);

  /**
   * when an exception occurs
   *
   * @param t
   */
  default void onException(Throwable t) {};

  /**
   * when the listening is finished
   */
  default void onFinish() {};
}

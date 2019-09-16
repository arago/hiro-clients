package co.arago.hiro.action.client;

import co.arago.hiro.action.client.builder.ActionClientBuilder;
import co.arago.hiro.client.builder.TokenBuilder;

/**
 *
 */
public class HiroAction {
  public static ActionClientBuilder newClient() {
    return new ActionClientBuilder();
  }

  public static TokenBuilder newToken() {
    return co.arago.hiro.client.Hiro.newToken();
  }

  private HiroAction() {
  }

}

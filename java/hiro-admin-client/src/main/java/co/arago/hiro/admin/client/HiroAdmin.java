package co.arago.hiro.admin.client;

import co.arago.hiro.admin.client.builder.AdminClientBuilder;
import co.arago.hiro.client.builder.TokenBuilder;

/**
 *
 */
public final class HiroAdmin
{
  public static AdminClientBuilder newClient()
  {
    return new AdminClientBuilder();
  }

  public static TokenBuilder newToken()
  {
    return co.arago.hiro.client.Hiro.newToken();
  }

  private HiroAdmin()
  {
  }
}

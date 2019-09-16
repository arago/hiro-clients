package co.arago.hiro.client.auth;

import java.util.Map;
import java.util.logging.Level;
import org.asynchttpclient.AsyncHttpClient;

import static co.arago.hiro.client.util.Helper.notEmpty;

/**
 *
 */
public final class PasswordTokenProvider extends AbstractTokenProvider {

  private static final String PASSWORD = "password";
  private static final String USERNAME = "username";

  private final String pass;
  private final String user;

  public PasswordTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts,
    Level debugLevel,
    String clientId, String clientSecret, String username, String password) {
    super(url, clientId, clientSecret, client, trustAllCerts, debugLevel);

    this.user = notEmpty(username, USERNAME);
    this.pass = notEmpty(password, PASSWORD);
  }

  public PasswordTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts, String clientId, String clientSecret, String username, String password) {
    this(url, client, trustAllCerts, null,
      clientId, clientSecret, username, password);
  }

  @Override
  protected void prepareTokenRequest(Map data) {
    data.put(USERNAME, user);
    data.put(PASSWORD, pass);
  }
}

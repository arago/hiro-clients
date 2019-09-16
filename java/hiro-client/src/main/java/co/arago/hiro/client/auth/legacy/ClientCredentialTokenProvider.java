package co.arago.hiro.client.auth.legacy;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;

/**
 *
 */
public final class ClientCredentialTokenProvider extends AbstractTokenProvider {

  private static final String CLIENT_CREDENTIALS = "client_credentials";

  public ClientCredentialTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts, String clientId, String clientSecret) {
    super(url, clientId, clientSecret, client, trustAllCerts);
  }

  @Override
  protected BoundRequestBuilder prepareTokenRequest(BoundRequestBuilder builder) {
    return builder;
  }

  @Override
  protected String getGrantType() {
    return CLIENT_CREDENTIALS;
  }

  @Override
  public boolean checkTokenRenewal(Response response) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean checkTokenRenewal(int httpResponseCode) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void renewToken() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}

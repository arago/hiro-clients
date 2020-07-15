package co.arago.hiro.client.auth.legacy;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;

import java.util.logging.Level;

import static co.arago.hiro.client.util.Helper.notEmpty;

/**
 *
 */
public final class PasswordTokenProvider extends AbstractTokenProvider {

    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";

    private final String pass;
    private final String user;

    public PasswordTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts, Level debugLevel,
            String clientId, String clientSecret, String username, String password) {
        super(url, clientId, clientSecret, client, trustAllCerts, debugLevel);

        this.user = notEmpty(username, USERNAME);
        this.pass = notEmpty(password, PASSWORD);
    }

    public PasswordTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts, String clientId,
            String clientSecret, String username, String password) {
        this(url, client, trustAllCerts, null, clientId, clientSecret, username, password);
    }

    @Override
    protected BoundRequestBuilder prepareTokenRequest(BoundRequestBuilder builder) {
        builder.addFormParam(USERNAME, user);
        builder.addFormParam(PASSWORD, pass);

        return builder;
    }

    @Override
    protected String getGrantType() {
        return PASSWORD;
    }

    @Override
    public boolean checkTokenRenewal(Response response) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public boolean checkTokenRenewal(int httpResponseCode) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public void renewToken() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

}

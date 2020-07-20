package co.arago.hiro.client.builder;

import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.auth.FixedTokenProvider;
import co.arago.hiro.client.auth.PasswordTokenProvider;
import org.asynchttpclient.AsyncHttpClient;

import static co.arago.hiro.client.util.Helper.notEmpty;
import java.util.logging.Level;

/**
 *
 */
public final class TokenBuilder {

    private AsyncHttpClient client;
    private boolean trustAllCerts;
    private Level debugLevel = Level.OFF;

    public TokenBuilder setClient(AsyncHttpClient client) {
        this.client = client;

        return this;

    }

    public TokenBuilder setDebugRest(Level level) {
        this.debugLevel = level;
        return this;
    }

    public TokenBuilder trustAllCerts() {
        this.trustAllCerts = true;

        return this;
    }

    public TokenProvider makeFixed(String token) {
        return new FixedTokenProvider(notEmpty(token, "token"));
    }

    public TokenProvider makePassword(String url, String clientId, String clientSecret, String userName,
            String password) {
        return new PasswordTokenProvider(notEmpty(url, "url"), client, trustAllCerts, debugLevel,
                notEmpty(clientId, "clientId"), notEmpty(clientSecret, "clientSecret"), notEmpty(userName, "userName"),
                notEmpty(password, "password"));
    }
}

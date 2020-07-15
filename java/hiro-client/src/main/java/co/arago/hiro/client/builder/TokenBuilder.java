package co.arago.hiro.client.builder;

import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.auth.DeviceTokenProvider;
import co.arago.hiro.client.auth.FixedTokenProvider;
import co.arago.hiro.client.auth.PasswordTokenProvider;
import org.asynchttpclient.AsyncHttpClient;

import java.util.logging.Level;

import static co.arago.hiro.client.auth.AbstractTokenProvider.DEFAULT_API_VERSION;
import static co.arago.hiro.client.util.Helper.notEmpty;

/**
 *
 */
public final class TokenBuilder {

    private AsyncHttpClient client;
    private boolean trustAllCerts;
    private Level debugLevel = Level.OFF;
    private String apiVersion = DEFAULT_API_VERSION;

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

    public TokenBuilder setApiVersion(String version) {
        this.apiVersion = version;

        return this;
    }

    public TokenProvider makeFixed(String token) {
        return new FixedTokenProvider(notEmpty(token, "token"));
    }

    public TokenProvider makePassword(String url, String clientId, String clientSecret, String userName,
            String password) {
        return new PasswordTokenProvider(notEmpty(url, "url"), client, trustAllCerts, debugLevel,
                notEmpty(clientId, "clientId"), notEmpty(clientSecret, "clientSecret"), notEmpty(userName, "userName"),
                notEmpty(password, "password"), notEmpty(apiVersion, "apiVErsion"));
    }

    public TokenProvider makeDevice(String url, String appId, String appSecret, String deviceId, String deviceSecret) {
        // NOTE: we do not pass apiVersion (device stuff has separate versioning)
        return new DeviceTokenProvider(notEmpty(url, "url"), client, trustAllCerts, debugLevel,
                notEmpty(appId, "appId"), notEmpty(appSecret, "appSecret"), notEmpty(deviceId, "deviceId"),
                notEmpty(deviceSecret, "deviceSecret"));
    }

}

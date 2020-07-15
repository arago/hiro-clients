package co.arago.hiro.client.auth.legacy;

import co.arago.hiro.client.api.TokenProvider;
import co.arago.hiro.client.util.HiroException;
import co.arago.hiro.client.util.HttpClientHelper;
import co.arago.hiro.client.util.Throwables;
import net.minidev.json.JSONValue;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static co.arago.hiro.client.util.Helper.notEmpty;

/**
 *
 */
public abstract class AbstractTokenProvider implements TokenProvider, Closeable {

    private static final Logger LOG = Logger.getLogger(AbstractTokenProvider.class.getName());
    protected static final String REFRESH_TOKEN = "refresh_token";
    protected static final String ACCESS_TOKEN = "access_token";
    protected static final String EXPIRES_IN = "expires_in";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CLIENT_ID = "client_id";

    private final int timeout = 60000;
    private final int invalidateTokenPeriod = 5000;
    private final int invalidateRefreshTokenPeriod = 300000;

    private final AsyncHttpClient client;
    private final String url;
    protected final String clientSecret;
    protected final String clientId;
    protected boolean debugRest = false;
    protected Level debugRestLevel = Level.OFF;

    private volatile String currentToken;
    private String refreshToken;
    private long invalidAfter = -1;

    public AbstractTokenProvider(String url, String clientId, String clientSecret, AsyncHttpClient client,
            boolean trustAllCerts) {
        this(url, clientId, clientSecret, client, trustAllCerts, null);
    }

    public AbstractTokenProvider(String url, String clientId, String clientSecret, AsyncHttpClient client,
            boolean trustAllCerts, Level debugLevel) {
        this.url = notEmpty(url, "url");
        this.clientId = notEmpty(clientId, "clientId");
        this.clientSecret = notEmpty(clientSecret, "clientSecret");
        this.client = client == null ? HttpClientHelper.newClient(trustAllCerts) : client;
        if (debugLevel != null && !Level.OFF.equals(debugLevel)) {
            this.debugRest = true;
            this.debugRestLevel = debugLevel;
        }
    }

    @Override
    public void revokeToken() {

    }

    @Override
    public void revokeToken(boolean resetState) {

    }

    @Override
    public void resetTokenState() {
        currentToken = null;
        refreshToken = null;
    }

    private synchronized void obtainToken() {
        final BoundRequestBuilder builder = prepareTokenRequest(newRequest(getGrantType()));

        try {
            if (debugRest) {
                HttpClientHelper.debugRequest(builder.build(), LOG, debugRestLevel);
            }

            final Response response = checkResponse(builder.execute().get(timeout, TimeUnit.MILLISECONDS));

            process(parseResponse(response));
        } catch (Exception ex) {
            Throwables.unchecked(ex);
        }
    }

    protected abstract BoundRequestBuilder prepareTokenRequest(BoundRequestBuilder builder);

    private synchronized void refreshToken() {
        if (refreshToken == null || refreshToken.isEmpty()) {
            obtainToken();
            return;
        }

        final BoundRequestBuilder builder = prepareRefreshTokenRequest(newRequest(REFRESH_TOKEN));

        try {
            final Response response = checkResponse(builder.execute().get(timeout, TimeUnit.MILLISECONDS));

            process(parseResponse(response));
        } catch (Exception ex) {
            obtainToken();
        }
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    private Response checkResponse(final Response response) {
        if (debugRest) {
            HttpClientHelper.debugResponse(response, LOG, debugRestLevel);
        }

        if (response.getStatusCode() != 200) {
            throw new HiroException("upstream error " + response.getStatusText(), response.getStatusCode());
        }
        return response;
    }

    private BoundRequestBuilder prepareRefreshTokenRequest(BoundRequestBuilder builder) {
        builder.addFormParam(REFRESH_TOKEN, refreshToken);

        return builder;
    }

    private void process(Map map) {
        if (map.get(EXPIRES_IN) != null) {
            this.invalidAfter = System.currentTimeMillis() + (((int) map.get(EXPIRES_IN)) * 1000);
        } else {
            this.invalidAfter = -1;
        }

        this.currentToken = notEmpty((String) map.get(ACCESS_TOKEN), "upstream access_token");
        this.refreshToken = (String) map.get(REFRESH_TOKEN);
    }

    @Override
    public final String toString() {
        return getToken();
    }

    @Override
    public final String getToken() {
        if (currentToken == null) {
            obtainToken();
        } else if (shouldRefresh()) {
            refreshToken();
        }

        return notEmpty(currentToken, "could not obtain token");
    }

    private Map parseResponse(Response response) throws Exception {
        final Object parsed = JSONValue.parse(response.getResponseBodyAsStream());
        // TODO check 503 statuscode
        if (!(parsed instanceof Map)) {
            throw new HiroException("invalid upstream response " + parsed, 503);
        }

        return (Map) parsed;
    }

    private boolean shouldRefresh() {
        if (invalidAfter < 0) {
            return false;
        }

        long now = System.currentTimeMillis();

        if (refreshToken != null && now >= invalidAfter - invalidateRefreshTokenPeriod) {
            return true;
        }

        return (now >= invalidAfter - invalidateTokenPeriod);
    }

    private BoundRequestBuilder newRequest(String grantType) {
        return client.preparePost(url).setHeader("Accept", "application/json")
                .setHeader("Content-type", "application/x-www-form-urlencoded;charset=UTF-8")
                .addFormParam(GRANT_TYPE, grantType).addFormParam(CLIENT_ID, clientId)
                .addFormParam(CLIENT_SECRET, clientSecret);
        // TODO If needed
        // .addFormParam("scope", getScope());

    }

    protected abstract String getGrantType();

    private String getScope() {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }
}

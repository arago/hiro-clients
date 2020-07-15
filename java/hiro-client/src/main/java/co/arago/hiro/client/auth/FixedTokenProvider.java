package co.arago.hiro.client.auth;

import co.arago.hiro.client.api.TokenProvider;
import org.asynchttpclient.Response;

import java.io.IOException;

import static co.arago.hiro.client.util.Helper.notEmpty;

/**
 *
 */
public final class FixedTokenProvider implements TokenProvider {
    private final String token;

    public FixedTokenProvider(String token) {
        this.token = notEmpty(token, "token");
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void close() throws IOException {
        //
    }

    @Override
    public void resetTokenState() {
    }

    @Override
    public void revokeToken() {
    }

    @Override
    public void revokeToken(boolean resetState) {
    }

    @Override
    public boolean checkTokenRenewal(Response response) {
        return false;
    }

    @Override
    public boolean checkTokenRenewal(int httpResponseCode) {
        return false;
    }

    @Override
    public void renewToken() {

    }

}

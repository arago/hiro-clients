package co.arago.hiro.client.api;

import java.io.Closeable;
import org.asynchttpclient.Response;

/**
 *
 */
public interface TokenProvider extends Token, Closeable {
    // marker interface

    void resetTokenState();

    /**
     * check the response, if 401 then renewToken() will be called
     * 
     * @param response
     * 
     * @return true if the token needed to be renewed
     */
    boolean checkTokenRenewal(Response response);

    boolean checkTokenRenewal(int httpResponseCode);

    void renewToken();

    void revokeToken();

    void revokeToken(boolean resetState);

}

package co.arago.hiro.client;

import co.arago.hiro.client.builder.ClientBuilder;
import co.arago.hiro.client.builder.TokenBuilder;

/**
 *
 */
public final class Hiro {
    /**
     * create a new client NOTE: the client is thread safe and must be reused/shared instead of being recreated all the
     * time
     * 
     * @return
     */
    public static ClientBuilder newClient() {
        return new ClientBuilder();
    }

    /**
     * create a new tokenprovider NOTE: a tokenprovider is valid for one HIRO client and must not be reused
     * 
     * @return
     */
    public static TokenBuilder newToken() {
        return new TokenBuilder();
    }

    private Hiro() {
    }
}

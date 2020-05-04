package co.arago.hiro.action.client.builder;

import co.arago.hiro.action.client.api.HiroActionClient;
import co.arago.hiro.action.client.rest.DefaultHiroActionClient;
import co.arago.hiro.client.builder.ClientBuilder;

import static co.arago.hiro.client.util.Helper.*;

public class ActionClientBuilder extends ClientBuilder {

    public HiroActionClient makeHiroActionClient() {
        return new DefaultHiroActionClient(notEmpty(restApiUrl, "restApiUrl"), notNull(tokenProvider, "tokenProvider"),
                client, trustAllCerts, debugLevel, timeout, apiVersion);
    }
}

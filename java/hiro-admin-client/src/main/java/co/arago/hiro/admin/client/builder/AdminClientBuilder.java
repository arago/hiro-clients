package co.arago.hiro.admin.client.builder;

import co.arago.hiro.admin.client.api.HiroAppClient;
import co.arago.hiro.admin.client.api.HiroEntitlementClient;
import co.arago.hiro.admin.client.api.HiroIamClient;
import co.arago.hiro.admin.client.rest.DefaultHiroAppClient;
import co.arago.hiro.admin.client.rest.DefaultHiroEntitlementClient;
import co.arago.hiro.admin.client.rest.DefaultHiroIamClient;
import co.arago.hiro.client.builder.ClientBuilder;

import static co.arago.hiro.client.util.Helper.*;

public class AdminClientBuilder extends ClientBuilder {

  public HiroIamClient makeHiroIamClient() {
    return new DefaultHiroIamClient(notEmpty(restApiUrl, "restApiUrl"),
      notNull(tokenProvider, "tokenProvider"), client,
      trustAllCerts, debugLevel, timeout, apiVersion);
  }

  public HiroAppClient makeHiroAppClient() {
    return new DefaultHiroAppClient(notEmpty(restApiUrl, "restApiUrl"),
      notNull(tokenProvider, "tokenProvider"), client,
      trustAllCerts, debugLevel, timeout, apiVersion);
  }

  public HiroEntitlementClient makeHiroEntitlementClient() {
    return new DefaultHiroEntitlementClient(notEmpty(restApiUrl, "restApiUrl"),
      notNull(tokenProvider, "tokenProvider"), client,
      trustAllCerts, debugLevel, timeout, apiVersion);
  }
}

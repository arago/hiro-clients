package co.arago.hiro.client.auth;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.proxy.ProxyServer;

import java.util.Map;
import java.util.logging.Level;

import static co.arago.hiro.client.util.Helper.notEmpty;

/**
 *
 */
public final class DeviceTokenProvider extends AbstractTokenProvider {

    private static final String DEVICE_ID = "device_id";
    private static final String DEVICE_SECRET = "device_secret";

    private final String deviceId;
    private final String deviceSecret;

    public DeviceTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts, Level debugLevel,
            String appId, String appSecret, String deviceId, String deviceSecret, String apiVersion, int timeout,
            ProxyServer.Builder proxyBuilder) {
        super(url, appId, appSecret, client, trustAllCerts, debugLevel, apiVersion, timeout, proxyBuilder);

        this.deviceId = notEmpty(deviceId, DEVICE_ID);
        this.deviceSecret = notEmpty(deviceSecret, DEVICE_SECRET);
    }

    public DeviceTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts, Level debugLevel,
            String appId, String appSecret, String deviceId, String deviceSecret, String apiVersion) {
        this(url, client, trustAllCerts, debugLevel, appId, appSecret, deviceId, deviceSecret, apiVersion, 0, null);
    }

    public DeviceTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts, Level debugLevel,
            String appId, String appSecret, String deviceId, String deviceSecret) {
        this(url, client, trustAllCerts, debugLevel, appId, appSecret, deviceId, deviceSecret, null, 0, null);
    }

    public DeviceTokenProvider(String url, AsyncHttpClient client, boolean trustAllCerts, String appId,
            String appSecret, String deviceId, String deviceSecret) {
        this(url, client, trustAllCerts, null, appId, appSecret, deviceId, deviceSecret, null, 0, null);
    }

    @Override
    protected void prepareTokenRequest(Map data) {
        data.put(DEVICE_ID, deviceId);
        data.put(DEVICE_SECRET, deviceSecret);
    }

    @Override
    protected String fullApiUrl() {
        return apiUrl + "/device";
    }

}

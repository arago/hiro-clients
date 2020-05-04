package co.arago.hiro.client.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Param;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

/**
 *
 */
public final class HttpClientHelper {
    private static final String USERAGENT = "co.arago.hiro.client/" + VersionHelper.version();
    public static Level HTTP_DEBUG_LEVEL = Level.FINEST;

    private static AsyncHttpClient _newClient(boolean trustAllCerts, int timeout) {
        final DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();

        if (trustAllCerts) {
            trustAll(builder);
        }

        builder.setUserAgent(USERAGENT);
        builder.setMaxRequestRetry(4);
        if (timeout > 0) {
            builder.setReadTimeout(timeout);
            builder.setRequestTimeout(timeout);
        }
        // timeouts not set
        // builder.setConnectTimeout(timeout)
        // builder.setHandshakeTimeout(timeout)
        // builder.setShutdownTimeout(timeout)

        return new DefaultAsyncHttpClient(builder.build());
    }

    public static AsyncHttpClient newClient(boolean trustAllCerts) {
        return _newClient(trustAllCerts, 0);
    }

    public static AsyncHttpClient newClient(boolean trustAllCerts, int timeout) {
        return _newClient(trustAllCerts, timeout);
    }

    private static void trustAll(DefaultAsyncHttpClientConfig.Builder builder) {
        try {
            builder.setUseInsecureTrustManager(true);
            builder.setDisableHttpsEndpointIdentificationAlgorithm(true);
        } catch (Throwable ex) {
            Throwables.unchecked(ex);
        }
    }

    private HttpClientHelper() {
    }

    public static void debugRequest(Request req, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            StringBuilder sb = new StringBuilder();
            sb.append("REQUEST [\n  Url=");
            sb.append(req.getUrl());
            sb.append("\n  Method=");
            sb.append(req.getMethod());
            sb.append("\n  Headers:\n");
            for (Entry<String, String> header : req.getHeaders()) {
                sb.append("    ");
                sb.append(header.getKey());
                sb.append(": ");
                sb.append(String.join(",", header.getValue()));
                sb.append("\n");
            }
            sb.append("  QueryParams:\n");
            for (Param param : req.getQueryParams()) {
                sb.append("    ");
                sb.append(param.getName());
                sb.append("=");
                sb.append(param.getValue());
                sb.append("\n");
            }
            sb.append("  FormParams:\n");
            for (Param param : req.getFormParams()) {
                sb.append("    ");
                sb.append(param.getName());
                sb.append("=");
                sb.append(param.getValue());
                sb.append("\n");
            }
            sb.append("  Body=[");
            sb.append(req.getStringData());
            sb.append("]\n]");
            logger.log(HTTP_DEBUG_LEVEL, sb.toString());
        }
    }

    public static void debugResponse(Response resp, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            StringBuilder sb = new StringBuilder();
            sb.append("RESPONSE [\n");
            sb.append("  Headers:\n");
            for (Map.Entry<String, String> header : resp.getHeaders()) {
                sb.append("    ");
                sb.append(header.getKey());
                sb.append(": ");
                sb.append(header.getValue());
                sb.append("\n");
            }
            sb.append("  StatusCode=");
            sb.append(resp.getStatusCode());
            sb.append("\n  StatusText=");
            sb.append(resp.getStatusText());
            sb.append("\n");
            String body = null;
            body = resp.getResponseBody();

            if (body != null) {
                sb.append("  Body=[");
                sb.append(body);
                sb.append("]\n");
            }
            sb.append("]");
            logger.log(HTTP_DEBUG_LEVEL, sb.toString());
        }
    }

}

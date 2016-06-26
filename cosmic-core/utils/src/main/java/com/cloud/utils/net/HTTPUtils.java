//

//

package com.cloud.utils.net;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HTTPUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPUtils.class);

    // The connection manager.
    private static final MultiThreadedHttpConnectionManager s_httpClientManager = new MultiThreadedHttpConnectionManager();

    private HTTPUtils() {
    }

    public static HttpClient getHTTPClient() {
        return new HttpClient(s_httpClientManager);
    }

    /**
     * @return A HttpMethodRetryHandler with given number of retries.
     */
    public static HttpMethodRetryHandler getHttpMethodRetryHandler(final int retryCount) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initializing new HttpMethodRetryHandler with retry count " + retryCount);
        }

        return new HttpMethodRetryHandler() {
            @Override
            public boolean retryMethod(final HttpMethod method, final IOException exception, final int executionCount) {
                if (executionCount >= retryCount) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    // Retry if the server dropped connection on us
                    return true;
                }
                if (!method.isRequestSent()) {
                    // Retry if the request has not been sent fully or
                    // if it's OK to retry methods that have been sent
                    return true;
                }
                // otherwise do not retry
                return false;
            }
        };
    }

    /**
     * @param proxy
     * @param httpClient
     */
    public static void setProxy(final Proxy proxy, final HttpClient httpClient) {
        if (proxy != null && httpClient != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Setting proxy with host " + proxy.getHost() + " and port " + proxy.getPort() + " for host " + httpClient.getHostConfiguration().getHost() + ":" +
                        httpClient.getHostConfiguration().getPort());
            }

            httpClient.getHostConfiguration().setProxy(proxy.getHost(), proxy.getPort());
            if (proxy.getUserName() != null && proxy.getPassword() != null) {
                httpClient.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword()));
            }
        }
    }

    /**
     * @param username
     * @param password
     * @param httpClient
     */
    public static void setCredentials(final String username, final String password, final HttpClient httpClient) {
        if (username != null && password != null && httpClient != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Setting credentials with username " + username + " for host " + httpClient.getHostConfiguration().getHost() + ":" + httpClient.getHostConfiguration
                        ().getPort());
            }

            httpClient.getParams().setAuthenticationPreemptive(true);
            httpClient.getState().setCredentials(
                    new AuthScope(httpClient.getHostConfiguration().getHost(), httpClient.getHostConfiguration().getPort(), AuthScope.ANY_REALM), new UsernamePasswordCredentials
                            (username, password));
        }
    }

    /**
     * @param httpClient
     * @param httpMethod
     * @return Returns the HTTP Status Code or -1 if an exception occurred.
     */
    public static int executeMethod(final HttpClient httpClient, final HttpMethod httpMethod) {
        // Execute GetMethod
        try {
            return httpClient.executeMethod(httpMethod);
        } catch (final IOException e) {
            LOGGER.warn("Exception while executing HttpMethod " + httpMethod.getName() + " on URL " + httpMethod.getPath());
            return -1;
        }
    }

    /**
     * @param responseCode
     * @return
     */
    public static boolean verifyResponseCode(final int responseCode) {
        switch (responseCode) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_MOVED_TEMPORARILY:
                return true;
            default:
                return false;
        }
    }
}

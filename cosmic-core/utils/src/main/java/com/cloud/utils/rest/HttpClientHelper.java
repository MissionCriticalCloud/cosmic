//

//

package com.cloud.utils.rest;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientHelper {

    static final Logger s_logger = LoggerFactory.getLogger(HttpClientHelper.class);

    private static final int MAX_ALLOCATED_CONNECTIONS = 50;
    private static final int MAX_ALLOCATED_CONNECTIONS_PER_ROUTE = 25;
    private static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 3000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    private static final String HTTPS = HttpConstants.HTTPS;

    public static CloseableHttpClient createHttpClient(final int maxRedirects) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        s_logger.info("Creating new HTTP connection pool and client");
        final Registry<ConnectionSocketFactory> socketFactoryRegistry = createSocketFactoryConfigration();
        final BasicCookieStore cookieStore = new BasicCookieStore();
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connManager.setDefaultMaxPerRoute(MAX_ALLOCATED_CONNECTIONS_PER_ROUTE);
        connManager.setMaxTotal(MAX_ALLOCATED_CONNECTIONS);
        final RequestConfig requestConfig = RequestConfig.custom()
                                                         .setCookieSpec(CookieSpecs.DEFAULT)
                                                         .setMaxRedirects(maxRedirects)
                                                         .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                                                         .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
                                                         .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                                                         .build();
        return HttpClientBuilder.create()
                                .setConnectionManager(connManager)
                                .setRedirectStrategy(new LaxRedirectStrategy())
                                .setDefaultRequestConfig(requestConfig)
                                .setDefaultCookieStore(cookieStore)
                                .setRetryHandler(new StandardHttpRequestRetryHandler())
                                .build();
    }

    private static Registry<ConnectionSocketFactory> createSocketFactoryConfigration() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        final Registry<ConnectionSocketFactory> socketFactoryRegistry;
        final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
        final SSLConnectionSocketFactory cnnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(HTTPS, cnnectionSocketFactory)
                .build();

        return socketFactoryRegistry;
    }
}

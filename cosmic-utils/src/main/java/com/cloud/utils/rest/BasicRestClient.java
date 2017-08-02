package com.cloud.utils.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicRestClient implements RestClient {

    private static final Logger s_logger = LoggerFactory.getLogger(BasicRestClient.class);

    private static final String HTTPS = HttpConstants.HTTPS;
    private static final int HTTPS_PORT = HttpConstants.HTTPS_PORT;

    private final CloseableHttpClient client;
    private final HttpClientContext clientContext;

    private BasicRestClient(final Builder<?> builder) {
        client = builder.client;
        clientContext = builder.clientContext;
        clientContext.setTargetHost(buildHttpHost(builder.host));
    }

    private static HttpHost buildHttpHost(final String host) {
        return new HttpHost(host, HTTPS_PORT, HTTPS);
    }

    protected BasicRestClient(final CloseableHttpClient client, final HttpClientContext clientContex, final String host) {
        this.client = client;
        clientContext = clientContex;
        clientContext.setTargetHost(buildHttpHost(host));
    }

    public static Builder create() {
        return new Builder();
    }

    @Override
    public CloseableHttpResponse execute(final HttpUriRequest request) throws CloudstackRESTException {
        logRequestExecution(request);
        try {
            return client.execute(clientContext.getTargetHost(), request, clientContext);
        } catch (final IOException e) {
            throw new CloudstackRESTException("Could not execute request " + request, e);
        }
    }

    private void logRequestExecution(final HttpUriRequest request) {
        final URI uri = request.getURI();
        String query = uri.getQuery();
        query = query != null ? "?" + query : "";
        s_logger.debug("Executig " + request.getMethod() + " request on " + clientContext.getTargetHost() + uri.getPath() + query);
    }

    @Override
    public void closeResponse(final CloseableHttpResponse response) throws CloudstackRESTException {
        try {
            s_logger.debug("Closing HTTP connection");
            response.close();
        } catch (final IOException e) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Failed to close response object for request.\nResponse: ").append(response);
            throw new CloudstackRESTException(sb.toString(), e);
        }
    }

    protected static class Builder<T extends Builder> {
        private CloseableHttpClient client;
        private HttpClientContext clientContext = HttpClientContext.create();
        private String host;

        public T client(final CloseableHttpClient client) {
            this.client = client;
            return (T) this;
        }

        public T clientContext(final HttpClientContext clientContext) {
            this.clientContext = clientContext;
            return (T) this;
        }

        public T host(final String host) {
            this.host = host;
            return (T) this;
        }

        public BasicRestClient build() {
            return new BasicRestClient(this);
        }
    }
}

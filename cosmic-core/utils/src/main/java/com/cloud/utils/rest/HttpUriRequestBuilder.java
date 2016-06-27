//

//

package com.cloud.utils.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import org.apache.http.Consts;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.springframework.util.Assert;

public class HttpUriRequestBuilder {

    private static final String CONTENT_TYPE = HttpConstants.CONTENT_TYPE;
    private static final String JSON_CONTENT_TYPE = HttpConstants.JSON_CONTENT_TYPE;

    private static final Optional<String> ABSENT = Optional.absent();
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, String> methodParameters = new HashMap<>();
    private HttpMethods method;
    private String path;
    private Optional<String> jsonPayload = ABSENT;

    private HttpUriRequestBuilder() {

    }

    public static HttpUriRequestBuilder create() {
        return new HttpUriRequestBuilder();
    }

    public HttpUriRequestBuilder method(final HttpMethods method) {
        this.method = method;
        return this;
    }

    public HttpUriRequestBuilder path(final String path) {
        this.path = path;
        return this;
    }

    public HttpUriRequestBuilder jsonPayload(final Optional<String> jsonPayload) {
        this.jsonPayload = jsonPayload;
        return this;
    }

    public HttpUriRequestBuilder parameters(final Map<String, String> parameters) {
        this.parameters.clear();
        this.parameters.putAll(parameters);
        return this;
    }

    public HttpUriRequestBuilder methodParameters(final Map<String, String> methodParameters) {
        this.methodParameters.clear();
        this.methodParameters.putAll(methodParameters);
        return this;
    }

    public HttpUriRequest build() {
        validate();
        final RequestBuilder builder = RequestBuilder.create(method.toString()).setUri(buildUri());
        if (!methodParameters.isEmpty()) {
            for (final Entry<String, String> entry : methodParameters.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        if (jsonPayload.isPresent()) {
            builder.addHeader(new BasicHeader(CONTENT_TYPE, JSON_CONTENT_TYPE))
                   .setEntity(new StringEntity(jsonPayload.get(), ContentType.create(JSON_CONTENT_TYPE, Consts.UTF_8)));
        }
        return builder.build();
    }

    private void validate() {
        Assert.notNull(method, "HTTP Method cannot be null");
        Assert.hasText(path, "target path must be defined");
        Assert.isTrue(path.startsWith("/"), "targte path must start with a '/' character");
    }

    private URI buildUri() {
        try {
            final URIBuilder builder = new URIBuilder().setPath(path);
            for (final Map.Entry<String, String> entry : parameters.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
            return builder.build();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Unable to build REST Service URI", e);
        }
    }
}

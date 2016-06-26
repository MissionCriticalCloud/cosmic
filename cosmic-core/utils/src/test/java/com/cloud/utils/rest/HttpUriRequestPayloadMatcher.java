//

//

package com.cloud.utils.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;

import java.io.IOException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class HttpUriRequestPayloadMatcher extends FeatureMatcher<HttpUriRequest, String> {

    public HttpUriRequestPayloadMatcher(final Matcher<? super String> subMatcher, final String featureDescription, final String featureName) {
        super(subMatcher, featureDescription, featureName);
    }

    public static HttpUriRequest aPayload(final String payload) {
        return argThat(hasPayload(payload));
    }

    public static HttpUriRequestPayloadMatcher hasPayload(final String payload) {
        return new HttpUriRequestPayloadMatcher(equalTo(payload), "payload", "payload");
    }

    @Override
    protected String featureValueOf(final HttpUriRequest actual) {
        String payload = "";
        if (actual instanceof HttpEntityEnclosingRequest) {
            try {
                payload = EntityUtils.toString(((HttpEntityEnclosingRequest) actual).getEntity());
            } catch (final ParseException e) {
                throw new IllegalArgumentException("Couldn't read request's entity payload.", e);
            } catch (final IOException e) {
                throw new IllegalArgumentException("Couldn't read request's entity payload.", e);
            }
        }
        return payload;
    }
}

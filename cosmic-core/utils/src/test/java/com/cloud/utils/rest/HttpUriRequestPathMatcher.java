//

//

package com.cloud.utils.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;

import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class HttpUriRequestPathMatcher extends FeatureMatcher<HttpUriRequest, String> {

    public HttpUriRequestPathMatcher(final Matcher<? super String> subMatcher, final String featureDescription, final String featureName) {
        super(subMatcher, featureDescription, featureName);
    }

    public static HttpUriRequest aPath(final String path) {
        return argThat(new HttpUriRequestPathMatcher(equalTo(path), "path", "path"));
    }

    @Override
    protected String featureValueOf(final HttpUriRequest actual) {
        return actual.getURI().getPath();
    }
}

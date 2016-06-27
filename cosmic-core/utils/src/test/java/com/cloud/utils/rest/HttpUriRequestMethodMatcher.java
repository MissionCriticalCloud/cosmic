//

//

package com.cloud.utils.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.argThat;

import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class HttpUriRequestMethodMatcher extends FeatureMatcher<HttpUriRequest, String> {

    public HttpUriRequestMethodMatcher(final Matcher<? super String> subMatcher, final String featureDescription, final String featureName) {
        super(subMatcher, featureDescription, featureName);
    }

    public static HttpUriRequest aMethod(final String method) {
        return argThat(new HttpUriRequestMethodMatcher(equalTo(method), "method", "method"));
    }

    @Override
    protected String featureValueOf(final HttpUriRequest actual) {
        return actual.getMethod();
    }
}

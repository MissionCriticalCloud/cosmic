package com.cloud.utils.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.http.HttpStatus;
import org.junit.Test;

public class HttpStatusCodeHelperTest {

    @Test
    public void testIsSuccess() throws Exception {
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_SWITCHING_PROTOCOLS), equalTo(false));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_PROCESSING), equalTo(false));

        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_OK), equalTo(true));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_CREATED), equalTo(true));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_ACCEPTED), equalTo(true));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION), equalTo(true));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_NO_CONTENT), equalTo(true));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_RESET_CONTENT), equalTo(true));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_PARTIAL_CONTENT), equalTo(true));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_MULTI_STATUS), equalTo(true));

        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_MULTIPLE_CHOICES), equalTo(false));
        assertThat(HttpStatusCodeHelper.isSuccess(HttpStatus.SC_MOVED_PERMANENTLY), equalTo(false));
    }

    @Test
    public void testIsUnauthorized() throws Exception {
        assertThat(HttpStatusCodeHelper.isUnauthorized(HttpStatus.SC_TEMPORARY_REDIRECT), equalTo(false));
        assertThat(HttpStatusCodeHelper.isUnauthorized(HttpStatus.SC_BAD_REQUEST), equalTo(false));

        assertThat(HttpStatusCodeHelper.isUnauthorized(HttpStatus.SC_UNAUTHORIZED), equalTo(true));

        assertThat(HttpStatusCodeHelper.isUnauthorized(HttpStatus.SC_PAYMENT_REQUIRED), equalTo(false));
        assertThat(HttpStatusCodeHelper.isUnauthorized(HttpStatus.SC_FORBIDDEN), equalTo(false));
    }
}

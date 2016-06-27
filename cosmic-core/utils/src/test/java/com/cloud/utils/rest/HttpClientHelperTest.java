//

//

package com.cloud.utils.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

public class HttpClientHelperTest {

    @Test
    public void testCreateClient() throws Exception {
        final int maxRedirects = 5;
        final CloseableHttpClient client = HttpClientHelper.createHttpClient(maxRedirects);

        assertThat(client, notNullValue());
    }
}

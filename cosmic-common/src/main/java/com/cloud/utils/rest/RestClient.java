package com.cloud.utils.rest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public interface RestClient {

    public CloseableHttpResponse execute(final HttpUriRequest request) throws CosmicRESTException;

    public void closeResponse(final CloseableHttpResponse response) throws CosmicRESTException;
}

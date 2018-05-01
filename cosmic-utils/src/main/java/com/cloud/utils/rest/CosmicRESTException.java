package com.cloud.utils.rest;

public class CosmicRESTException extends Exception {

    public CosmicRESTException(final String message) {
        super(message);
    }

    public CosmicRESTException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

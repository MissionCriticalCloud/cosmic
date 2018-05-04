package com.cloud.legacymodel.exceptions;

public class DiscoveryException extends CloudException {
    public DiscoveryException(final String msg) {
        this(msg, null);
    }

    public DiscoveryException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}

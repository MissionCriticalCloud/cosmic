package com.cloud.exception;

public class DiscoveredWithErrorException extends DiscoveryException {
    public DiscoveredWithErrorException(final String msg) {
        this(msg, null);
    }

    public DiscoveredWithErrorException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}

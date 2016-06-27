package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

public class DiscoveryException extends CloudException {

    private static final long serialVersionUID = SerialVersionUID.DiscoveryException;

    public DiscoveryException(final String msg) {
        this(msg, null);
    }

    public DiscoveryException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}

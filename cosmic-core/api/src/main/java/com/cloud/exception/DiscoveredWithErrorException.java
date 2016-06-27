package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

public class DiscoveredWithErrorException extends DiscoveryException {

    private static final long serialVersionUID = SerialVersionUID.DiscoveredWithErrorException;

    public DiscoveredWithErrorException(final String msg) {
        this(msg, null);
    }

    public DiscoveredWithErrorException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}

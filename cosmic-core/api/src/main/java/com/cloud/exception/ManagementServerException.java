package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

public class ManagementServerException extends CloudException {

    private static final long serialVersionUID = SerialVersionUID.ManagementServerException;

    public ManagementServerException() {

    }

    public ManagementServerException(final String message) {
        super(message);
    }

    public ManagementServerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

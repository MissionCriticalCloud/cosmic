package com.cloud.legacymodel.exceptions;

public class ManagementServerException extends CloudException {
    public ManagementServerException() {
    }

    public ManagementServerException(final String message) {
        super(message);
    }

    public ManagementServerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

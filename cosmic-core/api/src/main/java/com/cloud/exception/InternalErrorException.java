package com.cloud.exception;

public class InternalErrorException extends ManagementServerException {
    public InternalErrorException(final String message) {
        super(message);
    }

    public InternalErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

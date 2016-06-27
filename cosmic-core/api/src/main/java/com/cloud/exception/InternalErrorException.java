package com.cloud.exception;

public class InternalErrorException extends ManagementServerException {

    private static final long serialVersionUID = -3070582946175427902L;

    public InternalErrorException(final String message) {
        super(message);
    }

    public InternalErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

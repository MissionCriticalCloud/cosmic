package com.cloud.exception;

/**
 * Exception thrown if number of requests is over api rate limit set.
 */
public class RequestLimitException extends PermissionDeniedException {
    protected RequestLimitException() {
        super();
    }

    public RequestLimitException(final String msg) {
        super(msg);
    }

    public RequestLimitException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}

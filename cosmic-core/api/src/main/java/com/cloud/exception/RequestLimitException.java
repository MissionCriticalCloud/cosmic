package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

/**
 * Exception thrown if number of requests is over api rate limit set.
 */
public class RequestLimitException extends PermissionDeniedException {

    private static final long serialVersionUID = SerialVersionUID.AccountLimitException;

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

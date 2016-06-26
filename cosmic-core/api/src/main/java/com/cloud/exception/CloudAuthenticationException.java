package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;
import com.cloud.utils.exception.CloudRuntimeException;

public class CloudAuthenticationException extends CloudRuntimeException {
    private static final long serialVersionUID = SerialVersionUID.CloudAuthenticationException;

    public CloudAuthenticationException(final String message) {
        super(message);
    }

    public CloudAuthenticationException(final String message, final Throwable th) {
        super(message, th);
    }
}

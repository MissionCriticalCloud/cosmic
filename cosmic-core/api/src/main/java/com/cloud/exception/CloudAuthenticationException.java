package com.cloud.exception;

import com.cloud.utils.exception.CloudRuntimeException;

public class CloudAuthenticationException extends CloudRuntimeException {
    public CloudAuthenticationException(final String message) {
        super(message);
    }
}

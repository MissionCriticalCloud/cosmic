package com.cloud.exception;

import com.cloud.utils.exception.CloudRuntimeException;

public class UnsupportedServiceException extends CloudRuntimeException {

    public UnsupportedServiceException(final String message) {
        super(message);
    }
}

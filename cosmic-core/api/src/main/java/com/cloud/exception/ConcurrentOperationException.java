package com.cloud.exception;

import com.cloud.utils.exception.CloudRuntimeException;

public class ConcurrentOperationException extends CloudRuntimeException {
    public ConcurrentOperationException(final String msg) {
        super(msg);
    }
}

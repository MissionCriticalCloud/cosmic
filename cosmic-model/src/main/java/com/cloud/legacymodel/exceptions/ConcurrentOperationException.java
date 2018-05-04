package com.cloud.legacymodel.exceptions;

public class ConcurrentOperationException extends CloudRuntimeException {
    public ConcurrentOperationException(final String msg) {
        super(msg);
    }
}

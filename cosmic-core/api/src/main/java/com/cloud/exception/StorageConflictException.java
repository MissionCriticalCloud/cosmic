package com.cloud.exception;

public class StorageConflictException extends ManagementServerException {

    private static final long serialVersionUID = -294905017911859479L;

    public StorageConflictException(final String message) {
        super(message);
    }
}

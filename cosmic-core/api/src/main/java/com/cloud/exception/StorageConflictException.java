package com.cloud.exception;

public class StorageConflictException extends ManagementServerException {
    public StorageConflictException(final String message) {
        super(message);
    }
}

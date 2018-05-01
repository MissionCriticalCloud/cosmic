package com.cloud.exception;

public class VirtualMachineMigrationException extends CloudException {
    public VirtualMachineMigrationException(final String message) {
        super(message);
    }

    public VirtualMachineMigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}

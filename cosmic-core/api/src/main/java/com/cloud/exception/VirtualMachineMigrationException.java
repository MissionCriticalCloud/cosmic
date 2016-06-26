package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

public class VirtualMachineMigrationException extends CloudException {
    private static final long serialVersionUID = SerialVersionUID.VirtualMachineMigrationException;

    public VirtualMachineMigrationException(final String message) {
        super(message);
    }
}

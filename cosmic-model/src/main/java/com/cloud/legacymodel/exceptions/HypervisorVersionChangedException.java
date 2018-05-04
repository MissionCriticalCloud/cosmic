package com.cloud.legacymodel.exceptions;

public class HypervisorVersionChangedException extends CloudRuntimeException {

    public HypervisorVersionChangedException(final String message) {
        super(message);
    }

    protected HypervisorVersionChangedException() {
        super();
    }
}

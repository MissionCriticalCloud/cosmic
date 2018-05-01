package com.cloud.utils.exception;

public class HypervisorVersionChangedException extends CloudRuntimeException {

    public HypervisorVersionChangedException(final String message) {
        super(message);
    }

    protected HypervisorVersionChangedException() {
        super();
    }
}

package com.cloud.exception;

public class InsufficientAddressCapacityException extends InsufficientNetworkCapacityException {
    public InsufficientAddressCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg, scope, id);
    }

    protected InsufficientAddressCapacityException() {
        super();
    }
}

package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

/**
 * Exception thrown when the end there's not enough ip addresses in the system.
 */
public class InsufficientAddressCapacityException extends InsufficientNetworkCapacityException {

    private static final long serialVersionUID = SerialVersionUID.InsufficientAddressCapacityException;

    public InsufficientAddressCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg, scope, id);
    }

    protected InsufficientAddressCapacityException() {
        super();
    }
}

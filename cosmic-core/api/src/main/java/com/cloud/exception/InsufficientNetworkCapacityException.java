package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

public class InsufficientNetworkCapacityException extends InsufficientCapacityException {
    private static final long serialVersionUID = SerialVersionUID.InsufficientAddressCapacityException;

    protected InsufficientNetworkCapacityException() {
        super();
    }

    public InsufficientNetworkCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg, scope, id);
    }
}

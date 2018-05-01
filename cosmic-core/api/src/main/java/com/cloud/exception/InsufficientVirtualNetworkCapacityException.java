package com.cloud.exception;

import com.cloud.dc.Pod;

public class InsufficientVirtualNetworkCapacityException extends InsufficientNetworkCapacityException {
    public InsufficientVirtualNetworkCapacityException(final String msg, final long podId) {
        this(msg, Pod.class, podId);
    }

    public InsufficientVirtualNetworkCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg, scope, id);
    }
}

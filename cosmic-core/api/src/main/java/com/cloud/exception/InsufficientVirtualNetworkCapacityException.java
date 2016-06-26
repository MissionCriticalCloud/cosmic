package com.cloud.exception;

import com.cloud.dc.Pod;
import com.cloud.utils.SerialVersionUID;

public class InsufficientVirtualNetworkCapacityException extends InsufficientNetworkCapacityException {
    private static final long serialVersionUID = SerialVersionUID.InsufficientVirtualNetworkCapacityException;

    public InsufficientVirtualNetworkCapacityException(final String msg, final long podId) {
        this(msg, Pod.class, podId);
    }

    public InsufficientVirtualNetworkCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg, scope, id);
    }
}

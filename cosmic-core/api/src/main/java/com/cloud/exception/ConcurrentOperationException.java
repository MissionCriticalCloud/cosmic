package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;
import com.cloud.utils.exception.CloudRuntimeException;

public class ConcurrentOperationException extends CloudRuntimeException {

    private static final long serialVersionUID = SerialVersionUID.ConcurrentOperationException;

    public ConcurrentOperationException(final String msg) {
        super(msg);
    }
}

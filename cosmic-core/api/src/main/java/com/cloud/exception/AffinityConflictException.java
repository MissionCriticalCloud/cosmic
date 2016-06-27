package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;
import com.cloud.utils.exception.CloudRuntimeException;

public class AffinityConflictException extends CloudRuntimeException {

    private static final long serialVersionUID = SerialVersionUID.AffinityConflictException;

    public AffinityConflictException(final String message) {
        super(message);
    }

    public AffinityConflictException(final String message, final Throwable th) {
        super(message, th);
    }
}

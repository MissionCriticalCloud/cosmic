package com.cloud.exception;

import com.cloud.utils.exception.CloudRuntimeException;

public class InvalidParameterValueException extends CloudRuntimeException {

    private static final long serialVersionUID = -2232066904895010203L;

    public InvalidParameterValueException(final String message) {
        super(message);
    }
}

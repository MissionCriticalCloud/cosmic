package com.cloud.exception;

import com.cloud.utils.exception.CloudRuntimeException;

public class MissingParameterValueException extends CloudRuntimeException {

    public MissingParameterValueException(final String message) {
        super(message);
    }
}

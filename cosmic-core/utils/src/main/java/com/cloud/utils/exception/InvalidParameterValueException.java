package com.cloud.utils.exception;

public class InvalidParameterValueException extends CloudRuntimeException {

    private static final long serialVersionUID = -2232066904895010203L;

    public InvalidParameterValueException(final String message) {
        super(message);
    }
}

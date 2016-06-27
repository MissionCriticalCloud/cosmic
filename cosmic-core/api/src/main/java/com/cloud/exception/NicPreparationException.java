package com.cloud.exception;

public class NicPreparationException extends InsufficientCapacityException {

    public NicPreparationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NicPreparationException(final String message) {
        super(message, null);
    }
}

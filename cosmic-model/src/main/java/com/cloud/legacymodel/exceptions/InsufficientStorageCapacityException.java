package com.cloud.legacymodel.exceptions;

/**
 * InsufficientStorageCapcityException is thrown when there's not enough
 * storage space to create the VM.
 */
public class InsufficientStorageCapacityException extends InsufficientCapacityException {
    public InsufficientStorageCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg, scope, id);
    }
}

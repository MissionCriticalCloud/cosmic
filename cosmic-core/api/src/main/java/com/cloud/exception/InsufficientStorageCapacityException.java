package com.cloud.exception;

import com.cloud.storage.StoragePool;
import com.cloud.utils.SerialVersionUID;

/**
 * InsufficientStorageCapcityException is thrown when there's not enough
 * storage space to create the VM.
 */
public class InsufficientStorageCapacityException extends InsufficientCapacityException {

    private static final long serialVersionUID = SerialVersionUID.InsufficientStorageCapacityException;

    public InsufficientStorageCapacityException(final String msg, final long id) {
        this(msg, StoragePool.class, id);
    }

    public InsufficientStorageCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg, scope, id);
    }
}

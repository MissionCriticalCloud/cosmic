package com.cloud.exception;

import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.vm.Nic;

public class NicPreparationException extends InsufficientCapacityException {

    public NicPreparationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NicPreparationException(final String message) {
        super(message, Nic.class, 0L);
    }
}

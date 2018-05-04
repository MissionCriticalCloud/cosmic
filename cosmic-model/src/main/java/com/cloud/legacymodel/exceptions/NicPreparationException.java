package com.cloud.legacymodel.exceptions;

import com.cloud.legacymodel.network.Nic;

public class NicPreparationException extends InsufficientCapacityException {

    public NicPreparationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NicPreparationException(final String message) {
        super(message, Nic.class, 0L);
    }
}

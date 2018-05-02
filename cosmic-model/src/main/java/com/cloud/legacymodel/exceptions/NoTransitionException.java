package com.cloud.legacymodel.exceptions;

/**
 * Thrown by the state machine when there is no transition from one state to another.
 */
public class NoTransitionException extends Exception {
    public NoTransitionException(final String msg) {
        super(msg);
    }
}

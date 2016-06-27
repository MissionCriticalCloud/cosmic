package org.apache.cloudstack.framework.events;

public class EventBusException extends Exception {
    public EventBusException(final String msg) {
        super(msg);
    }

    public EventBusException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

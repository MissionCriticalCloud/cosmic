package com.cloud.hypervisor.ovm3.objects;

public class Ovm3ResourceException extends Exception {
    private static final long serialVersionUID = 1L;
    private static final Throwable CAUSE = null;

    public Ovm3ResourceException() {
        super();
    }

    public Ovm3ResourceException(final String message) {
        super(message);
    }

    public Ovm3ResourceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Override
    public Throwable getCause() {
        return CAUSE;
    }
}

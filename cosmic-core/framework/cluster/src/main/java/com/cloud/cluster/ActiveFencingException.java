package com.cloud.cluster;

public class ActiveFencingException extends Exception {
    private static final long serialVersionUID = -3975376101728211726L;

    public ActiveFencingException(final String message) {
        super(message);
    }

    public ActiveFencingException(final String message, final Throwable th) {
        super(message, th);
    }
}

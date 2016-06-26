package com.cloud.cluster;

public class ClusterInvalidSessionException extends Exception {

    private static final long serialVersionUID = -6636524194520997512L;

    public ClusterInvalidSessionException(final String message) {
        super(message);
    }

    public ClusterInvalidSessionException(final String message, final Throwable th) {
        super(message, th);
    }
}

package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

/**
 * Generic parent exception class for capacity being reached.
 */
public abstract class InsufficientCapacityException extends CloudException {
    private static final long serialVersionUID = SerialVersionUID.InsufficientCapacityException;

    Long id;
    Class<?> scope;

    protected InsufficientCapacityException() {
        super();
    }

    public InsufficientCapacityException(final String msg, final Class<?> scope, final Long id) {
        super(msg);
        this.scope = scope;
        this.id = id;
    }

    public InsufficientCapacityException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @return scope where we are insufficient.  The possible classes are
     * Host, StoragePool, Cluster, Pod, DataCenter, NetworkConfiguration.
     */
    public Class<?> getScope() {
        return scope;
    }

    /**
     * @return the id of the object that it is insufficient in.  Note that this method is
     * marked such that if the id is not set, then it will throw NullPointerException.
     * This is intended as you should check to see if the Scope is present before
     * accessing this method.
     */
    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        final String str = super.toString();
        return str + "Scope=" + scope + "; id=" + id;
    }
}

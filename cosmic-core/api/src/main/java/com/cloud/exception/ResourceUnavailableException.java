package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

public class ResourceUnavailableException extends CloudException {
    private static final long serialVersionUID = SerialVersionUID.ResourceUnavailableException;

    Class<?> _scope;
    long _id;

    public ResourceUnavailableException(final String msg, final Class<?> scope, final long resourceId) {
        this(msg, scope, resourceId, null);
    }

    public ResourceUnavailableException(final String msg, final Class<?> scope, final long resourceId, final Throwable cause) {
        super(new StringBuilder("Resource [").append(scope.getSimpleName()).append(":").append(resourceId).append("] is unreachable: ").append(msg).toString(), cause);
        _scope = scope;
        _id = resourceId;
    }

    public Class<?> getScope() {
        return _scope;
    }

    public long getResourceId() {
        return _id;
    }
}

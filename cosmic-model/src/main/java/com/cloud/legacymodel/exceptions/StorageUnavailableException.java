package com.cloud.legacymodel.exceptions;

import com.cloud.legacymodel.storage.StoragePool;

/**
 * If the cause is due to storage pool unavailable, calling
 * problem with.
 */
public class StorageUnavailableException extends ResourceUnavailableException {
    public StorageUnavailableException(final String msg, final long poolId) {
        this(msg, poolId, null);
    }

    public StorageUnavailableException(final String msg, final long poolId, final Throwable cause) {
        this(msg, StoragePool.class, poolId, cause);
    }

    public StorageUnavailableException(final String msg, final Class<?> scope, final long resourceId, final Throwable th) {
        super(msg, scope, resourceId, th);
    }

    public StorageUnavailableException(final String msg, final Class<?> scope, final long resourceId) {
        this(msg, scope, resourceId, null);
    }
}

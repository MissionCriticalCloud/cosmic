package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.exception.StorageConflictException;

public interface HypervisorHostListener {
    boolean hostConnect(long hostId, long poolId) throws StorageConflictException;

    boolean hostDisconnected(long hostId, long poolId);
}

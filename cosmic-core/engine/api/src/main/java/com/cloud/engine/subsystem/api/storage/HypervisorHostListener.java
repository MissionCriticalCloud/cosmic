package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.exceptions.StorageConflictException;

public interface HypervisorHostListener {
    boolean hostConnect(long hostId, long poolId) throws StorageConflictException;

    boolean hostDisconnected(long hostId, long poolId);
}

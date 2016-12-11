package com.cloud.storage;

import com.cloud.engine.subsystem.api.storage.DataStore;

public interface StoragePoolAutomation {
    public boolean maintain(DataStore store);

    public boolean cancelMaintain(DataStore store);
}

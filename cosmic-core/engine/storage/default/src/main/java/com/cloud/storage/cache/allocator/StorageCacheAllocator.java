package com.cloud.storage.cache.allocator;

import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.Scope;

public interface StorageCacheAllocator {
    DataStore getCacheStore(Scope scope);

    DataStore getCacheStore(DataObject data, Scope scope);
}

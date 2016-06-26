package org.apache.cloudstack.storage.cache.allocator;

import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;

public interface StorageCacheAllocator {
    DataStore getCacheStore(Scope scope);

    DataStore getCacheStore(DataObject data, Scope scope);
}

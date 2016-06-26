package org.apache.cloudstack.engine.subsystem.api.storage;

public interface StorageCacheManager {
    DataStore getCacheStorage(Scope scope);

    DataStore getCacheStorage(DataObject data, Scope scope);

    DataObject createCacheObject(DataObject data, Scope scope);

    /**
     * only create cache object in db
     *
     * @param data
     * @param scope
     * @return
     */
    DataObject getCacheObject(DataObject data, Scope scope);

    boolean deleteCacheObject(DataObject data);

    boolean releaseCacheObject(DataObject data);

    DataObject createCacheObject(DataObject data, DataStore store);
}

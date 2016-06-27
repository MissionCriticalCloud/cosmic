package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.DataStoreRole;

import java.util.List;

public interface DataStoreManager {
    DataStore getDataStore(long storeId, DataStoreRole role);

    DataStore getPrimaryDataStore(long storeId);

    DataStore getPrimaryDataStore(String storeUuid);

    DataStore getDataStore(String uuid, DataStoreRole role);

    List<DataStore> getImageStoresByScope(ZoneScope scope);

    DataStore getImageStore(long zoneId);

    List<DataStore> getImageCacheStores(Scope scope);

    DataStore getImageCacheStore(long zoneId);

    List<DataStore> listImageStores();

    List<DataStore> listImageCacheStores();

    boolean isRegionStore(DataStore store);
}

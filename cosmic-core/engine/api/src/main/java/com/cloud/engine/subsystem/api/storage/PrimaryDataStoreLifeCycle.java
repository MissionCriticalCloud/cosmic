package com.cloud.engine.subsystem.api.storage;

import com.cloud.storage.StoragePool;

import java.util.Map;

public interface PrimaryDataStoreLifeCycle extends DataStoreLifeCycle {
    String CAPACITY_BYTES = "capacityBytes";
    String CAPACITY_IOPS = "capacityIops";

    void updateStoragePool(StoragePool storagePool, Map<String, String> details);

    void enableStoragePool(DataStore store);

    void disableStoragePool(DataStore store);
}

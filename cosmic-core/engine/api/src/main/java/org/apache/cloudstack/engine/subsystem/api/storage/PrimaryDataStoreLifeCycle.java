package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.StoragePool;

import java.util.Map;

public interface PrimaryDataStoreLifeCycle extends DataStoreLifeCycle {
    public static final String CAPACITY_BYTES = "capacityBytes";
    public static final String CAPACITY_IOPS = "capacityIops";

    void updateStoragePool(StoragePool storagePool, Map<String, String> details);

    void enableStoragePool(DataStore store);

    void disableStoragePool(DataStore store);
}

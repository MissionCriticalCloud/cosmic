package com.cloud.storage.datastore;

import com.cloud.engine.subsystem.api.storage.HypervisorHostListener;
import com.cloud.engine.subsystem.api.storage.PrimaryDataStore;
import com.cloud.engine.subsystem.api.storage.PrimaryDataStoreDriver;

public interface PrimaryDataStoreProviderManager {
    PrimaryDataStore getPrimaryDataStore(long dataStoreId);

    PrimaryDataStore getPrimaryDataStore(String uuid);

    boolean registerDriver(String providerName, PrimaryDataStoreDriver driver);

    boolean registerHostListener(String providerName, HypervisorHostListener listener);
}

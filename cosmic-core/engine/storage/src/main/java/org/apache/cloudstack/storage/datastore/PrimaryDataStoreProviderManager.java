package org.apache.cloudstack.storage.datastore;

import org.apache.cloudstack.engine.subsystem.api.storage.HypervisorHostListener;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreDriver;

public interface PrimaryDataStoreProviderManager {
    public PrimaryDataStore getPrimaryDataStore(long dataStoreId);

    public PrimaryDataStore getPrimaryDataStore(String uuid);

    boolean registerDriver(String providerName, PrimaryDataStoreDriver driver);

    boolean registerHostListener(String providerName, HypervisorHostListener listener);
}

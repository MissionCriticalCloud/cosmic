package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.DataStoreProviderApiService;
import com.cloud.utils.component.Manager;

public interface DataStoreProviderManager extends Manager, DataStoreProviderApiService {
    DataStoreProvider getDataStoreProvider(String name);

    DataStoreProvider getDefaultPrimaryDataStoreProvider();

    DataStoreProvider getDefaultImageDataStoreProvider();

    DataStoreProvider getDefaultCacheDataStoreProvider();
}

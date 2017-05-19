package com.cloud.engine.subsystem.api.storage;

import java.util.Map;
import java.util.Set;

public interface DataStoreProvider {
    String NFS_IMAGE = "NFS";
    String DEFAULT_PRIMARY = "DefaultPrimary";

    DataStoreLifeCycle getDataStoreLifeCycle();

    DataStoreDriver getDataStoreDriver();

    HypervisorHostListener getHostListener();

    String getName();

    boolean configure(Map<String, Object> params);

    Set<DataStoreProviderType> getTypes();

    enum DataStoreProviderType {
        PRIMARY, IMAGE, ImageCache
    }
}

package org.apache.cloudstack.engine.subsystem.api.storage;

import java.util.Map;
import java.util.Set;

public interface DataStoreProvider {
    // constants for provider names
    String NFS_IMAGE = "NFS";
    String S3_IMAGE = "S3";
    String SWIFT_IMAGE = "Swift";
    String SAMPLE_IMAGE = "Sample";
    String SMB = "NFS";
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

package com.cloud.engine.subsystem.api.storage;

import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.model.enumeration.StoragePoolType;

import java.util.Map;

public interface PrimaryDataStoreInfo extends StoragePool {
    static final String MANAGED = "managed";
    static final String STORAGE_HOST = "storageHost";
    static final String STORAGE_PORT = "storagePort";
    static final String MANAGED_STORE_TARGET = "managedStoreTarget";
    static final String MANAGED_STORE_TARGET_ROOT_VOLUME = "managedStoreTargetRootVolume";
    static final String CHAP_INITIATOR_USERNAME = "chapInitiatorUsername";
    static final String CHAP_INITIATOR_SECRET = "chapInitiatorSecret";
    static final String CHAP_TARGET_USERNAME = "chapTargetUsername";
    static final String CHAP_TARGET_SECRET = "chapTargetSecret";
    static final String VOLUME_SIZE = "volumeSize";

    @Override
    String getUuid();

    @Override
    StoragePoolType getPoolType();

    boolean isManaged();

    Map<String, String> getDetails();

    void setDetails(Map<String, String> details);
}

package com.cloud.legacymodel.storage;

import com.cloud.model.enumeration.StoragePoolType;

import java.util.Map;

public interface PrimaryDataStoreInfo extends StoragePool {
    String MANAGED = "managed";
    String STORAGE_HOST = "storageHost";
    String STORAGE_PORT = "storagePort";
    String MANAGED_STORE_TARGET = "managedStoreTarget";
    String MANAGED_STORE_TARGET_ROOT_VOLUME = "managedStoreTargetRootVolume";
    String CHAP_INITIATOR_USERNAME = "chapInitiatorUsername";
    String CHAP_INITIATOR_SECRET = "chapInitiatorSecret";
    String CHAP_TARGET_USERNAME = "chapTargetUsername";
    String CHAP_TARGET_SECRET = "chapTargetSecret";
    String VOLUME_SIZE = "volumeSize";

    @Override
    String getUuid();

    @Override
    StoragePoolType getPoolType();

    boolean isManaged();

    Map<String, String> getDetails();

    void setDetails(Map<String, String> details);
}

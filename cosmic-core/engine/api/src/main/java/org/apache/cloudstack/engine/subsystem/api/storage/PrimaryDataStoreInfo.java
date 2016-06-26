package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StoragePool;
import org.apache.cloudstack.engine.subsystem.api.storage.disktype.DiskFormat;

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

    boolean isHypervisorSupported(HypervisorType hypervisor);

    boolean isLocalStorageSupported();

    boolean isVolumeDiskTypeSupported(DiskFormat diskType);

    @Override
    String getUuid();

    @Override
    StoragePoolType getPoolType();

    boolean isManaged();

    Map<String, String> getDetails();

    void setDetails(Map<String, String> details);

    PrimaryDataStoreLifeCycle getLifeCycle();
}

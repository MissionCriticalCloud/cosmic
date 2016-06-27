package com.cloud.storage;

import com.cloud.hypervisor.Hypervisor;
import com.cloud.storage.Storage.StoragePoolType;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface StoragePool extends Identity, InternalIdentity {

    /**
     * @return name of the pool.
     */
    String getName();

    /**
     * @return the type of pool.
     */
    StoragePoolType getPoolType();

    /**
     * @return the date the pool first registered
     */
    Date getCreated();

    /**
     * @return the last time the state of this pool was modified.
     */
    Date getUpdateTime();

    /**
     * @return availability zone.
     */
    long getDataCenterId();

    /**
     * @return capacity of storage poolin bytes
     */
    long getCapacityBytes();

    /**
     * @return available storage in bytes
     */
    long getUsedBytes();

    Long getCapacityIops();

    Long getClusterId();

    /**
     * @return the fqdn or ip address of the storage host
     */
    String getHostAddress();

    /**
     * @return the filesystem path of the pool on the storage host (server)
     */
    String getPath();

    /**
     * @return the user information / credentials for the storage host
     */
    String getUserInfo();

    /**
     * @return the storage pool represents a shared storage resource
     */
    boolean isShared();

    /**
     * @return the storage pool represents a local storage resource
     */
    boolean isLocal();

    /**
     * @return the storage pool status
     */
    StoragePoolStatus getStatus();

    int getPort();

    Long getPodId();

    String getStorageProviderName();

    boolean isInMaintenance();

    Hypervisor.HypervisorType getHypervisor();
}

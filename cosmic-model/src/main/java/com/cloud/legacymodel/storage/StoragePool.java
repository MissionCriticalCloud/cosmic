package com.cloud.legacymodel.storage;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.StoragePoolStatus;
import com.cloud.model.enumeration.StoragePoolType;

import java.util.Date;

public interface StoragePool extends Identity, InternalIdentity {

    String getName();

    StoragePoolType getPoolType();

    Date getCreated();

    Date getUpdateTime();

    long getDataCenterId();

    long getCapacityBytes();

    long getUsedBytes();

    Long getCapacityIops();

    Long getClusterId();

    String getHostAddress();

    String getPath();

    String getUserInfo();

    boolean isShared();

    boolean isZoneWide();

    boolean isClusterWide();

    boolean isLocal();

    StoragePoolStatus getStatus();

    int getPort();

    Long getPodId();

    String getStorageProviderName();

    boolean isInMaintenance();

    HypervisorType getHypervisor();
}

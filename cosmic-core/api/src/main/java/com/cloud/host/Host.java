package com.cloud.host;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.resource.ResourceState;
import com.cloud.legacymodel.statemachine.StateObject;
import com.cloud.model.enumeration.HostType;
import com.cloud.model.enumeration.HypervisorType;

import java.util.Date;

public interface Host extends StateObject<HostStatus>, Identity, InternalIdentity {
    String getName();

    HostType getType();

    Date getCreated();

    HostStatus getStatus();

    String getPrivateIpAddress();

    String getStorageUrl();

    String getStorageIpAddress();

    String getGuid();

    Long getTotalMemory();

    Integer getCpuSockets();

    Integer getCpus();

    Integer getProxyPort();

    Long getPodId();

    long getDataCenterId();

    String getParent();

    String getStorageIpAddressDeux();

    HypervisorType getHypervisorType();

    Date getDisconnectedOn();

    String getVersion();

    long getTotalSize();

    String getCapabilities();

    long getLastPinged();

    Long getManagementServerId();

    Date getRemoved();

    Long getClusterId();

    String getPublicIpAddress();

    String getPublicNetmask();

    String getPrivateNetmask();

    String getStorageNetmask();

    String getStorageMacAddress();

    String getPublicMacAddress();

    String getPrivateMacAddress();

    String getStorageNetmaskDeux();

    String getStorageMacAddressDeux();

    String getHypervisorVersion();

    boolean isInMaintenanceStates();

    ResourceState getResourceState();
}

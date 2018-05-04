package com.cloud.capacity;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

public interface Capacity extends InternalIdentity, Identity {
    short CAPACITY_TYPE_MEMORY = 0;
    short CAPACITY_TYPE_CPU = 1;
    short CAPACITY_TYPE_STORAGE = 2;
    short CAPACITY_TYPE_STORAGE_ALLOCATED = 3;
    short CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP = 4;
    short CAPACITY_TYPE_PRIVATE_IP = 5;
    short CAPACITY_TYPE_SECONDARY_STORAGE = 6;
    short CAPACITY_TYPE_VLAN = 7;
    short CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP = 8;
    short CAPACITY_TYPE_LOCAL_STORAGE = 9;
    short CAPACITY_TYPE_GPU = 19;

    Long getHostOrPoolId();

    Long getDataCenterId();

    Long getPodId();

    Long getClusterId();

    long getUsedCapacity();

    long getTotalCapacity();

    short getCapacityType();

    long getReservedCapacity();

    Float getUsedPercentage();
}

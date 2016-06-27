package com.cloud.capacity;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface Capacity extends InternalIdentity, Identity {
    public static final short CAPACITY_TYPE_MEMORY = 0;
    public static final short CAPACITY_TYPE_CPU = 1;
    public static final short CAPACITY_TYPE_STORAGE = 2;
    public static final short CAPACITY_TYPE_STORAGE_ALLOCATED = 3;
    public static final short CAPACITY_TYPE_VIRTUAL_NETWORK_PUBLIC_IP = 4;
    public static final short CAPACITY_TYPE_PRIVATE_IP = 5;
    public static final short CAPACITY_TYPE_SECONDARY_STORAGE = 6;
    public static final short CAPACITY_TYPE_VLAN = 7;
    public static final short CAPACITY_TYPE_DIRECT_ATTACHED_PUBLIC_IP = 8;
    public static final short CAPACITY_TYPE_LOCAL_STORAGE = 9;
    public static final short CAPACITY_TYPE_GPU = 19;

    public Long getHostOrPoolId();

    public Long getDataCenterId();

    public Long getPodId();

    public Long getClusterId();

    public long getUsedCapacity();

    public long getTotalCapacity();

    public short getCapacityType();

    public long getReservedCapacity();

    public Float getUsedPercentage();
}

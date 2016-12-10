package com.cloud.network;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.network.Networks.TrafficType;

public interface PhysicalNetworkTrafficType extends InternalIdentity, Identity {

    long getPhysicalNetworkId();

    TrafficType getTrafficType();

    String getXenNetworkLabel();

    String getKvmNetworkLabel();

    String getOvm3NetworkLabel();
}

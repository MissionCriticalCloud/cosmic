package com.cloud.network;

import com.cloud.network.Networks.TrafficType;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface PhysicalNetworkTrafficType extends InternalIdentity, Identity {

    long getPhysicalNetworkId();

    TrafficType getTrafficType();

    String getXenNetworkLabel();

    String getKvmNetworkLabel();

    String getOvm3NetworkLabel();
}

package com.cloud.network;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.TrafficType;

public interface PhysicalNetworkTrafficType extends InternalIdentity, Identity {

    long getPhysicalNetworkId();

    TrafficType getTrafficType();

    String getXenNetworkLabel();

    String getKvmNetworkLabel();
}

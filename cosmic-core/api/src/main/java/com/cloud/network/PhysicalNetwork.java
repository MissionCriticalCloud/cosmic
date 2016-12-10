package com.cloud.network;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.utils.Pair;

import java.util.List;

/**
 *
 */
public interface PhysicalNetwork extends Identity, InternalIdentity {

    BroadcastDomainRange getBroadcastDomainRange();

    long getDataCenterId();

    State getState();

    List<String> getTags();

    List<String> getIsolationMethods();

    Long getDomainId();

    List<Pair<Integer, Integer>> getVnet();

    String getVnetString();

    String getSpeed();

    String getName();

    enum State {
        Disabled, Enabled
    }

    enum IsolationMethod {
        VLAN, L3, GRE, STT, BCF_SEGMENT, MIDO, SSP, VXLAN, ODL, L3VPN, VSP, VCS
    }

    enum BroadcastDomainRange {
        POD, ZONE
    }
}

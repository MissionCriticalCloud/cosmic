package com.cloud.network;

import com.cloud.utils.Pair;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.List;

/**
 *
 */
public interface PhysicalNetwork extends Identity, InternalIdentity {

    BroadcastDomainRange getBroadcastDomainRange();

    long getDataCenterId();

    State getState();

    List<String> getTags();

    // TrafficType getTrafficType();

    List<String> getIsolationMethods();

    Long getDomainId();

    List<Pair<Integer, Integer>> getVnet();

    String getVnetString();

    String getSpeed();

    String getName();

    public enum State {
        Disabled, Enabled
    }

    public enum IsolationMethod {
        VLAN, L3, GRE, STT, BCF_SEGMENT, MIDO, SSP, VXLAN, ODL, L3VPN, VSP, VCS
    }

    public enum BroadcastDomainRange {
        POD, ZONE
    }
}

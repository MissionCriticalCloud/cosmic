package com.cloud.network.vpc;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface VpcGateway extends Identity, ControlledEntity, InternalIdentity {
    /**
     * @return
     */
    String getIp4Address();

    /**
     * @return
     */
    Type getType();

    /**
     * @return
     */
    Long getVpcId();

    /**
     * @return
     */
    long getZoneId();

    /**
     * @return
     */
    long getNetworkId();

    /**
     * @return
     */
    String getGateway();

    /**
     * @return
     */
    String getNetmask();

    /**
     * @return
     */
    String getBroadcastUri();

    /**
     * @return
     */
    State getState();

    /**
     * @return
     */
    boolean getSourceNat();

    /**
     * @return
     */
    long getNetworkACLId();

    public enum Type {
        Private, Public, Vpn
    }

    public enum State {
        Creating, Ready, Deleting
    }
}

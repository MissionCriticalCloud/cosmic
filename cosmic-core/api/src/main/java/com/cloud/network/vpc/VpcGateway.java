package com.cloud.network.vpc;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

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
    State getState();

    /**
     * @return
     */
    boolean getSourceNat();

    /**
     * @return
     */
    long getNetworkACLId();

    enum Type {
        Private
    }

    enum State {
        Creating, Ready, Deleting
    }
}

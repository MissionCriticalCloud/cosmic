package com.cloud.legacymodel.network.vpc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

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

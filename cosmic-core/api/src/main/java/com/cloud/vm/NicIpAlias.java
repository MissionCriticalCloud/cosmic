package com.cloud.vm;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

/**
 * Each entry represents the alis ip of a perticular nic.
 */
public interface NicIpAlias extends ControlledEntity, Identity, InternalIdentity {
    @Override
    long getId();

    long getNicId();

    String getIp4Address();

    String getIp6Address();

    long getNetworkId();

    long getVmId();

    Long getAliasCount();

    String getNetmask();

    String getGateway();

    /**
     * @return id in the CloudStack database
     */
    enum State {
        active, revoked,
    }
}

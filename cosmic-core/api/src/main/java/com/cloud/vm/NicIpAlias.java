package com.cloud.vm;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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

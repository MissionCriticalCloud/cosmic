package com.cloud.vm;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

/**
 * Nic represents one nic on the VM.
 */
public interface NicSecondaryIp extends ControlledEntity, Identity, InternalIdentity {
    /**
     * @return id in the CloudStack database
     */
    @Override
    long getId();

    long getNicId();

    String getIp4Address();

    long getNetworkId();

    long getVmId();
}

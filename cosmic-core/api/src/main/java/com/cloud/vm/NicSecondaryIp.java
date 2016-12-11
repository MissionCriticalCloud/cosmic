package com.cloud.vm;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

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

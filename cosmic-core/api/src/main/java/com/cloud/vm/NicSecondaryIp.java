package com.cloud.vm;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

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

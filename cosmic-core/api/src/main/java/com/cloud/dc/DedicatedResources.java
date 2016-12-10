package com.cloud.dc;

import com.cloud.acl.InfrastructureEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface DedicatedResources extends InfrastructureEntity, InternalIdentity, Identity {
    @Override
    long getId();

    Long getDataCenterId();

    Long getPodId();

    Long getClusterId();

    Long getHostId();

    Long getDomainId();

    Long getAccountId();

    @Override
    String getUuid();

    long getAffinityGroupId();
}

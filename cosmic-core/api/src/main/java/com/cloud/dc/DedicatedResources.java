package com.cloud.dc;

import org.apache.cloudstack.acl.InfrastructureEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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

package com.cloud.legacymodel.dc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

public interface DedicatedResources extends InternalIdentity, Identity {
    Long getDataCenterId();

    Long getPodId();

    Long getClusterId();

    Long getHostId();

    Long getDomainId();

    Long getAccountId();

    long getAffinityGroupId();
}

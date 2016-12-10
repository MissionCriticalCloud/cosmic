package com.cloud.dc;

import com.cloud.acl.InfrastructureEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface StorageNetworkIpRange extends InfrastructureEntity, InternalIdentity, Identity {

    Integer getVlan();

    String getPodUuid();

    String getStartIp();

    String getEndIp();

    String getNetworkUuid();

    String getZoneUuid();

    String getNetmask();

    String getGateway();
}

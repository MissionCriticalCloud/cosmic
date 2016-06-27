package com.cloud.dc;

import org.apache.cloudstack.acl.InfrastructureEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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

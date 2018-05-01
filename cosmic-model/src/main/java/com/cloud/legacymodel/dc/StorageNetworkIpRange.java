package com.cloud.legacymodel.dc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

public interface StorageNetworkIpRange extends InternalIdentity, Identity {

    Integer getVlan();

    String getPodUuid();

    String getStartIp();

    String getEndIp();

    String getNetworkUuid();

    String getZoneUuid();

    String getNetmask();

    String getGateway();
}

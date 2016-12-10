package com.cloud.region;

import com.cloud.acl.InfrastructureEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface PortableIpRange extends InfrastructureEntity, InternalIdentity, Identity {

    public final static String UNTAGGED = "untagged";

    public String getVlanTag();

    public String getGateway();

    public String getNetmask();

    public int getRegionId();

    public String getIpRange();
}

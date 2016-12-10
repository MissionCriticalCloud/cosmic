package com.cloud.region;

import com.cloud.acl.InfrastructureEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface PortableIpRange extends InfrastructureEntity, InternalIdentity, Identity {

    public final static String UNTAGGED = "untagged";

    public String getVlanTag();

    public String getGateway();

    public String getNetmask();

    public int getRegionId();

    public String getIpRange();
}

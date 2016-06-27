package com.cloud.network.vpc;

import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface NetworkACL extends InternalIdentity, Identity, Displayable {
    public static final long DEFAULT_DENY = 1;
    public static final long DEFAULT_ALLOW = 2;

    String getDescription();

    String getUuid();

    Long getVpcId();

    @Override
    long getId();

    String getName();

    @Override
    boolean isDisplay();
}

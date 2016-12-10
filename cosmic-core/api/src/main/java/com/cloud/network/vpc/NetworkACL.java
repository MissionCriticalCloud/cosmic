package com.cloud.network.vpc;

import com.cloud.api.Displayable;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

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

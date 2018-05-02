package com.cloud.legacymodel.network.vpc;

import com.cloud.legacymodel.Displayable;
import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

public interface NetworkACL extends InternalIdentity, Identity, Displayable {
    long DEFAULT_DENY = 1;
    long DEFAULT_ALLOW = 2;

    String getDescription();

    String getUuid();

    Long getVpcId();

    @Override
    long getId();

    String getName();

    @Override
    boolean isDisplay();
}

package com.cloud.affinity;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface AffinityGroup extends ControlledEntity, InternalIdentity, Identity {

    String getName();

    String getDescription();

    String getType();

    ACLType getAclType();
}

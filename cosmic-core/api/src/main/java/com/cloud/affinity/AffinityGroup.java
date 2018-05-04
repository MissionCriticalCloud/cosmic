package com.cloud.affinity;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

public interface AffinityGroup extends ControlledEntity, InternalIdentity, Identity {

    String getName();

    String getDescription();

    String getType();

    ACLType getAclType();
}

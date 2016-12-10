package com.cloud.affinity;

import com.cloud.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface AffinityGroup extends ControlledEntity, InternalIdentity, Identity {

    String getName();

    String getDescription();

    String getType();

    ACLType getAclType();
}

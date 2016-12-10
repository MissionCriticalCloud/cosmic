package com.cloud.network.security;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

//FIXME: Annotation @doc("")
public interface SecurityGroup extends ControlledEntity, InternalIdentity, Identity {

    String getName();

    String getDescription();
}

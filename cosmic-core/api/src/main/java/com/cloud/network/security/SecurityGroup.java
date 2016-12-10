package com.cloud.network.security;

import com.cloud.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

//FIXME: Annotation @doc("")
public interface SecurityGroup extends ControlledEntity, InternalIdentity, Identity {

    String getName();

    String getDescription();
}

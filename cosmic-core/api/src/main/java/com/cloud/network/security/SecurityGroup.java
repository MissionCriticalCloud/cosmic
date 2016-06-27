package com.cloud.network.security;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

//FIXME: Annotation @doc("")
public interface SecurityGroup extends ControlledEntity, InternalIdentity, Identity {

    String getName();

    String getDescription();
}

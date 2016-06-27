package com.cloud.vm;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface InstanceGroup extends ControlledEntity, Identity, InternalIdentity {

    String getName();

    Date getCreated();

    Short getAccountType();
}

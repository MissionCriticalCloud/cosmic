package com.cloud.vm;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

import java.util.Date;

public interface InstanceGroup extends ControlledEntity, Identity, InternalIdentity {

    String getName();

    Date getCreated();

    Short getAccountType();
}

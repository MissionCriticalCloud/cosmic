package com.cloud.vm;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

import java.util.Date;

public interface InstanceGroup extends ControlledEntity, Identity, InternalIdentity {

    String getName();

    Date getCreated();

    Short getAccountType();
}

package com.cloud.network.as;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface AutoScaleVmGroup extends ControlledEntity, InternalIdentity, Displayable {

    String State_New = "new";
    String State_Revoke = "revoke";
    String State_Enabled = "enabled";
    String State_Disabled = "disabled";

    @Override
    long getId();

    @Override
    long getAccountId();

    Long getLoadBalancerId();

    long getProfileId();

    int getMinMembers();

    int getMaxMembers();

    int getMemberPort();

    int getInterval();

    Date getLastInterval();

    String getState();

    String getUuid();

    @Override
    boolean isDisplay();
}

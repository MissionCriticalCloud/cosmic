package com.cloud.network.as;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface AutoScalePolicy extends ControlledEntity, InternalIdentity {

    @Override
    long getId();

    String getUuid();

    public int getDuration();

    public int getQuietTime();

    public Date getLastQuiteTime();

    public String getAction();
}

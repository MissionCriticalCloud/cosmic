package com.cloud.event;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface Event extends ControlledEntity, Identity, InternalIdentity {
    String getType();

    State getState();

    String getDescription();

    Date getCreateDate();

    long getUserId();

    int getTotalSize();

    String getLevel();

    long getStartId();

    String getParameters();

    boolean getArchived();

    public enum State {
        Created, Scheduled, Started, Completed
    }
}

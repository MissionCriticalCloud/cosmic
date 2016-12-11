package com.cloud.event;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

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

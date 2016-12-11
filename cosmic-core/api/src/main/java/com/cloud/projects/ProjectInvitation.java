package com.cloud.projects;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

import java.util.Date;

public interface ProjectInvitation extends ControlledEntity, Identity, InternalIdentity {
    long getProjectId();

    Long getForAccountId();

    String getToken();

    String getEmail();

    Date getCreated();

    State getState();

    Long getInDomainId();

    public enum State {
        Pending, Completed, Expired, Declined
    }
}

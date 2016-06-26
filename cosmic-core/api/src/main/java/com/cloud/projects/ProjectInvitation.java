package com.cloud.projects;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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

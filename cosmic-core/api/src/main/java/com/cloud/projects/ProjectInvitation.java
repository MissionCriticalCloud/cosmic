package com.cloud.projects;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

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

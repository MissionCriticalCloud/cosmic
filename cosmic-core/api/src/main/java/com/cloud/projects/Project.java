package com.cloud.projects;

import com.cloud.domain.PartOf;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface Project extends PartOf, Identity, InternalIdentity {
    String getDisplayText();

    @Override
    long getDomainId();

    @Override
    long getId();

    Date getCreated();

    Date getRemoved();

    String getName();

    long getProjectAccountId();

    State getState();

    void setState(State state);

    public enum State {
        Active, Disabled, Suspended
    }

    public enum ListProjectResourcesCriteria {
        ListProjectResourcesOnly, SkipProjectResources
    }
}

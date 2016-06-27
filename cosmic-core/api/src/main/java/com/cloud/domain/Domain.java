package com.cloud.domain;

import com.cloud.user.OwnedBy;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

/**
 * Domain defines the Domain object.
 */

public interface Domain extends OwnedBy, Identity, InternalIdentity {
    public static final long ROOT_DOMAIN = 1L;

    Long getParent();

    void setParent(Long parent);

    String getName();

    void setName(String name);

    Date getRemoved();

    String getPath();

    void setPath(String path);

    int getLevel();

    int getChildCount();

    long getNextChildSeq();

    State getState();

    void setState(State state);

    String getNetworkDomain();

    enum State {
        Active, Inactive
    }
}

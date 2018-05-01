package com.cloud.domain;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.user.OwnedBy;

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

    String getEmail();

    enum State {
        Active, Inactive
    }
}

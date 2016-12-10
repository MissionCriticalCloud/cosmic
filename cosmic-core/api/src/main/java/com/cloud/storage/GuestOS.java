package com.cloud.storage;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

import java.util.Date;

public interface GuestOS extends InternalIdentity, Identity {

    String getName();

    String getDisplayName();

    long getCategoryId();

    Date getCreated();

    Date getRemoved();

    boolean getIsUserDefined();
}

package com.cloud.storage;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

import java.util.Date;

public interface GuestOS extends InternalIdentity, Identity {

    String getName();

    String getDisplayName();

    long getCategoryId();

    Date getCreated();

    Date getRemoved();

    boolean getIsUserDefined();
}

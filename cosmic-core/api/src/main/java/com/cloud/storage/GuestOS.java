package com.cloud.storage;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface GuestOS extends InternalIdentity, Identity {

    String getName();

    String getDisplayName();

    long getCategoryId();

    Date getCreated();

    Date getRemoved();

    boolean getIsUserDefined();
}

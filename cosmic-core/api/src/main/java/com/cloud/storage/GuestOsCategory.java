package com.cloud.storage;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface GuestOsCategory extends Identity, InternalIdentity {
    // Used by OS preference, 'None' for no OS preference
    public static final String CATEGORY_NONE = "None";

    String getName();

    void setName(String name);
}

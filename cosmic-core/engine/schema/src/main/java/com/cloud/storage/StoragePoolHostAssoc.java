package com.cloud.storage;

import com.cloud.api.InternalIdentity;

import java.util.Date;

public interface StoragePoolHostAssoc extends InternalIdentity {

    long getHostId();

    long getPoolId();

    String getLocalPath();

    Date getCreated();

    Date getLastUpdated();
}

package com.cloud.storage;

import com.cloud.legacymodel.InternalIdentity;

import java.util.Date;

public interface StoragePoolHostAssoc extends InternalIdentity {

    long getHostId();

    long getPoolId();

    String getLocalPath();

    Date getCreated();

    Date getLastUpdated();
}

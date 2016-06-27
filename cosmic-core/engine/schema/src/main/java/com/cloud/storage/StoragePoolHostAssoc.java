package com.cloud.storage;

import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface StoragePoolHostAssoc extends InternalIdentity {

    long getHostId();

    long getPoolId();

    String getLocalPath();

    Date getCreated();

    Date getLastUpdated();
}

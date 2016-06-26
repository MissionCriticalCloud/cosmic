package com.cloud.storage;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface ImageStore extends Identity, InternalIdentity {

    /**
     * @return name of the object store.
     */
    String getName();

    /**
     * @return availability zone.
     */
    Long getDataCenterId();

    /**
     * @return object store provider name
     */
    String getProviderName();

    /**
     * @return data store protocol
     */
    String getProtocol();
}

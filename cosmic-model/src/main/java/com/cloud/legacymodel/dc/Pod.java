package com.cloud.legacymodel.dc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.AllocationState;

/**
 * Represents one pod in the Cosmic.
 */
public interface Pod extends Identity, InternalIdentity {

    String getCidrAddress();

    int getCidrSize();

    String getGateway();

    long getDataCenterId();

    String getDescription();

    String getName();

    AllocationState getAllocationState();

    boolean belongsToDataCenter(final long dataCenterId);
}

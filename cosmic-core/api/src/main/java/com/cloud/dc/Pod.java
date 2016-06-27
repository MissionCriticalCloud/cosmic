package com.cloud.dc;

import com.cloud.org.Grouping;
import org.apache.cloudstack.acl.InfrastructureEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

/**
 * Represents one pod in the cloud stack.
 */
public interface Pod extends InfrastructureEntity, Grouping, Identity, InternalIdentity {

    String getCidrAddress();

    int getCidrSize();

    String getGateway();

    long getDataCenterId();

    String getDescription();

    String getName();

    AllocationState getAllocationState();

    boolean getExternalDhcp();

    boolean belongsToDataCenter(final long dataCenterId);
}

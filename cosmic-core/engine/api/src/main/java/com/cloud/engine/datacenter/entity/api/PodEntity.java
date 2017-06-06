package com.cloud.engine.datacenter.entity.api;

import com.cloud.model.enumeration.AllocationState;

public interface PodEntity extends DataCenterResourceEntity {

    String getCidrAddress();

    int getCidrSize();

    String getGateway();

    long getDataCenterId();

    AllocationState getAllocationState();
}

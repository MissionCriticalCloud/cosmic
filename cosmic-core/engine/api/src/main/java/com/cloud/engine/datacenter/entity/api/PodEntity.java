package com.cloud.engine.datacenter.entity.api;

import com.cloud.org.Grouping.AllocationState;

public interface PodEntity extends DataCenterResourceEntity {

    String getCidrAddress();

    int getCidrSize();

    String getGateway();

    long getDataCenterId();

    AllocationState getAllocationState();
}

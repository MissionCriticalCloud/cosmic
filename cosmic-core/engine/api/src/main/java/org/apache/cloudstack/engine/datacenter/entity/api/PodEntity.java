package org.apache.cloudstack.engine.datacenter.entity.api;

import com.cloud.org.Cluster;
import com.cloud.org.Grouping.AllocationState;

import java.util.List;

public interface PodEntity extends DataCenterResourceEntity {

    List<Cluster> listClusters();

    String getCidrAddress();

    int getCidrSize();

    String getGateway();

    long getDataCenterId();

    AllocationState getAllocationState();

    boolean getExternalDhcp();
}

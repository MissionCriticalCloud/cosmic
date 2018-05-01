package com.cloud.deploy;

import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.vm.ReservationContext;

public interface DeploymentPlan {
    long getDataCenterId();

    Long getPodId();

    Long getClusterId();

    Long getHostId();

    Long getPoolId();

    ExcludeList getAvoids();

    void setAvoids(ExcludeList avoids);

    Long getPhysicalNetworkId();

    void setPhysicalNetworkId(Long physicalNetworkId);

    ReservationContext getReservationContext();
}

package com.cloud.deploy;

import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.vm.ReservationContext;

/**
 */
public interface DeploymentPlan {
    // TODO: This interface is not fully developed. It really
    // number of parameters to be specified.

    /**
     * @return data center the VM should deploy in.
     */
    public long getDataCenterId();

    /**
     * @return pod the Vm should deploy in; null if no preference.
     */
    public Long getPodId();

    /**
     * @return cluster the VM should deploy in; null if no preference.
     */
    public Long getClusterId();

    /**
     * @return host the VM should deploy in; null if no preference.
     */
    public Long getHostId();

    /**
     * @return pool the VM should be created in; null if no preference.
     */
    public Long getPoolId();

    /**
     * @return the ExcludeList to avoid for deployment
     */
    public ExcludeList getAvoids();

    /**
     * @param avoids Set the ExcludeList to avoid for deployment
     */
    public void setAvoids(ExcludeList avoids);

    Long getPhysicalNetworkId();

    ReservationContext getReservationContext();
}

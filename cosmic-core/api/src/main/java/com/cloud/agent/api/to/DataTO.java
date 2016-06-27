package com.cloud.agent.api.to;

import com.cloud.hypervisor.Hypervisor;

public interface DataTO {
    public DataObjectType getObjectType();

    public DataStoreTO getDataStore();

    public Hypervisor.HypervisorType getHypervisorType();

    /**
     * @return
     */
    String getPath();

    long getId();
}

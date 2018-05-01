package com.cloud.agent.api.to;

import com.cloud.model.enumeration.HypervisorType;

public interface DataTO {
    public DataObjectType getObjectType();

    public DataStoreTO getDataStore();

    public HypervisorType getHypervisorType();

    /**
     * @return
     */
    String getPath();

    long getId();
}

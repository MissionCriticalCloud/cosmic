package com.cloud.legacymodel.to;

import com.cloud.model.enumeration.DataObjectType;
import com.cloud.model.enumeration.HypervisorType;

public interface DataTO {
    DataObjectType getObjectType();

    DataStoreTO getDataStore();

    HypervisorType getHypervisorType();

    String getPath();

    long getId();
}

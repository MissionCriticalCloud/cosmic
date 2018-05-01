package com.cloud.engine.subsystem.api.storage;

import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.model.enumeration.HypervisorType;

import java.util.Map;

public interface DataStoreLifeCycle {
    DataStore initialize(Map<String, Object> dsInfos);

    boolean attachCluster(DataStore store, ClusterScope scope);

    boolean attachHost(DataStore store, HostScope scope, StoragePoolInfo existingInfo);

    boolean attachZone(DataStore dataStore, ZoneScope scope, HypervisorType hypervisorType);

    boolean maintain(DataStore store);

    boolean cancelMaintain(DataStore store);

    boolean deleteDataStore(DataStore store);
}

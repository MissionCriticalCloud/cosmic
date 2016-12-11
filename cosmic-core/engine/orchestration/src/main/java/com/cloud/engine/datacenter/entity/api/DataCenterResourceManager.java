package com.cloud.engine.datacenter.entity.api;

import com.cloud.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import com.cloud.engine.datacenter.entity.api.db.EngineClusterVO;
import com.cloud.engine.datacenter.entity.api.db.EngineDataCenterVO;
import com.cloud.engine.datacenter.entity.api.db.EngineHostPodVO;
import com.cloud.engine.datacenter.entity.api.db.EngineHostVO;
import com.cloud.utils.fsm.NoTransitionException;

public interface DataCenterResourceManager {

    EngineDataCenterVO loadDataCenter(String dataCenterId);

    void saveDataCenter(EngineDataCenterVO dc);

    void savePod(EngineHostPodVO dc);

    void saveCluster(EngineClusterVO cluster);

    boolean changeState(DataCenterResourceEntity entity, Event event) throws NoTransitionException;

    EngineHostPodVO loadPod(String uuid);

    EngineClusterVO loadCluster(String uuid);

    EngineHostVO loadHost(String uuid);

    void saveHost(EngineHostVO hostVO);
}

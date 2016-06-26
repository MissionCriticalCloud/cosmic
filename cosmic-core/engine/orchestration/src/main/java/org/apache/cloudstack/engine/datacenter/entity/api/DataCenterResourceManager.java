package org.apache.cloudstack.engine.datacenter.entity.api;

import com.cloud.utils.fsm.NoTransitionException;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineClusterVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineDataCenterVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostPodVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO;

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

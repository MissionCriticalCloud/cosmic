package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostPodVO;

import java.util.HashMap;
import java.util.List;

public interface EngineHostPodDao extends GenericDao<EngineHostPodVO, Long>,
        StateDao<DataCenterResourceEntity.State, DataCenterResourceEntity.State.Event, DataCenterResourceEntity> {
    List<EngineHostPodVO> listByDataCenterId(long id);

    EngineHostPodVO findByName(String name, long dcId);

    HashMap<Long, List<Object>> getCurrentPodCidrSubnets(long zoneId, long podIdToSkip);

    List<Long> listDisabledPods(long zoneId);
}

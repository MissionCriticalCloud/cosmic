package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineClusterVO;

import java.util.List;
import java.util.Map;

public interface EngineClusterDao extends GenericDao<EngineClusterVO, Long>,
        StateDao<DataCenterResourceEntity.State, DataCenterResourceEntity.State.Event, DataCenterResourceEntity> {
    List<EngineClusterVO> listByPodId(long podId);

    EngineClusterVO findBy(String name, long podId);

    List<EngineClusterVO> listByHyTypeWithoutGuid(String hyType);

    List<EngineClusterVO> listByZoneId(long zoneId);

    List<HypervisorType> getAvailableHypervisorInZone(Long zoneId);

    List<EngineClusterVO> listByDcHyType(long dcId, String hyType);

    Map<Long, List<Long>> getPodClusterIdMap(List<Long> clusterIds);

    List<Long> listDisabledClusters(long zoneId, Long podId);

    List<Long> listClustersWithDisabledPods(long zoneId);
}

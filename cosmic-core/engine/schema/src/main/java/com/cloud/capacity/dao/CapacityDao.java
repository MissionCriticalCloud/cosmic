package com.cloud.capacity.dao;

import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDaoImpl.SummedCapacity;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Map;

public interface CapacityDao extends GenericDao<CapacityVO, Long> {
    CapacityVO findByHostIdType(Long hostId, short capacityType);

    List<Long> listClustersInZoneOrPodByHostCapacities(long id, int requiredCpu, long requiredRam, short capacityTypeForOrdering, boolean isZone);

    List<Long> listHostsWithEnoughCapacity(int requiredCpu, long requiredRam, Long clusterId, String hostType);

    boolean removeBy(Short capacityType, Long zoneId, Long podId, Long clusterId, Long hostId);

    List<SummedCapacity> findByClusterPodZone(Long zoneId, Long podId, Long clusterId);

    List<SummedCapacity> findNonSharedStorageForClusterPodZone(Long zoneId, Long podId, Long clusterId);

    Pair<List<Long>, Map<Long, Double>> orderClustersByAggregateCapacity(long id, short capacityType, boolean isZone);

    List<SummedCapacity> findCapacityBy(Integer capacityType, Long zoneId, Long podId, Long clusterId);

    List<Long> listPodsByHostCapacities(long zoneId, int requiredCpu, long requiredRam, short capacityType);

    Pair<List<Long>, Map<Long, Double>> orderPodsByAggregateCapacity(long zoneId, short capacityType);

    List<SummedCapacity> findCapacityBy(Integer capacityType, Long zoneId,
                                        Long podId, Long clusterId, String resourceState);

    List<SummedCapacity> listCapacitiesGroupedByLevelAndType(Integer capacityType, Long zoneId, Long podId, Long clusterId, int level, Long limit);

    void updateCapacityState(Long dcId, Long podId, Long clusterId,
                             Long hostId, String capacityState);

    List<Long> listClustersCrossingThreshold(short capacityType, Long zoneId, String configName, long computeRequested);

    float findClusterConsumption(Long clusterId, short capacityType, long computeRequested);

    List<Long> orderHostsByFreeCapacity(Long clusterId, short capacityType);
}

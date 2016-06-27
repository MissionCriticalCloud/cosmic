package com.cloud.dc.dao;

import com.cloud.dc.ClusterVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Map;

public interface ClusterDao extends GenericDao<ClusterVO, Long> {
    List<ClusterVO> listByPodId(long podId);

    ClusterVO findBy(String name, long podId);

    List<ClusterVO> listByHyTypeWithoutGuid(String hyType);

    List<ClusterVO> listByZoneId(long zoneId);

    List<HypervisorType> getAvailableHypervisorInZone(Long zoneId);

    List<ClusterVO> listByDcHyType(long dcId, String hyType);

    Map<Long, List<Long>> getPodClusterIdMap(List<Long> clusterIds);

    List<Long> listDisabledClusters(long zoneId, Long podId);

    List<Long> listClustersWithDisabledPods(long zoneId);

    List<ClusterVO> listClustersByDcId(long zoneId);

    List<Long> listAllCusters(long zoneId);
}

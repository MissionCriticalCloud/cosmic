package com.cloud.dc.dao;

import com.cloud.dc.DedicatedResourceVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface DedicatedResourceDao extends GenericDao<DedicatedResourceVO, Long> {

    DedicatedResourceVO findByZoneId(Long zoneId);

    DedicatedResourceVO findByPodId(Long podId);

    DedicatedResourceVO findByClusterId(Long clusterId);

    DedicatedResourceVO findByHostId(Long hostId);

    Pair<List<DedicatedResourceVO>, Integer> searchDedicatedHosts(Long hostId, Long domainId, Long accountId, Long affinityGroupId);

    Pair<List<DedicatedResourceVO>, Integer> searchDedicatedClusters(Long clusterId, Long domainId, Long accountId, Long affinityGroupId);

    Pair<List<DedicatedResourceVO>, Integer> searchDedicatedPods(Long podId, Long domainId, Long accountId, Long affinityGroupId);

    Pair<List<DedicatedResourceVO>, Integer> searchDedicatedZones(Long dataCenterId, Long domainId, Long accountId, Long affinityGroupId);

    List<DedicatedResourceVO> listByAccountId(Long accountId);

    List<DedicatedResourceVO> listByDomainId(Long domainId);

    List<DedicatedResourceVO> listZonesNotInDomainIds(List<Long> domainIds);

    List<Long> listAllPods();

    List<Long> listAllClusters();

    List<Long> listAllHosts();

    List<DedicatedResourceVO> listByAffinityGroupId(Long affinityGroupId);
}

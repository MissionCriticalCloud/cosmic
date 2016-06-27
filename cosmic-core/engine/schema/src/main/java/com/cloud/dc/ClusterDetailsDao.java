package com.cloud.dc;

import com.cloud.utils.db.GenericDao;

import java.util.Map;

public interface ClusterDetailsDao extends GenericDao<ClusterDetailsVO, Long> {
    Map<String, String> findDetails(long clusterId);

    void persist(long clusterId, Map<String, String> details);

    void persist(long clusterId, String name, String value);

    ClusterDetailsVO findDetail(long clusterId, String name);

    void deleteDetails(long clusterId);

    String getVmwareDcName(Long clusterId);
}

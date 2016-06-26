package com.cloud.dc.dao;

import com.cloud.dc.ClusterVSMMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ClusterVSMMapDao extends GenericDao<ClusterVSMMapVO, Long> {
    ClusterVSMMapVO findByClusterId(long clusterId);

    List<ClusterVSMMapVO> listByVSMId(long vsmId);

    boolean removeByVsmId(long vsmId);

    boolean removeByClusterId(long clusterId);
}

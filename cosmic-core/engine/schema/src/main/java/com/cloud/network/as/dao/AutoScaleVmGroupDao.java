package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScaleVmGroupVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AutoScaleVmGroupDao extends GenericDao<AutoScaleVmGroupVO, Long> {
    List<AutoScaleVmGroupVO> listByAll(Long loadBalancerId, Long profileId);

    boolean isProfileInUse(long profileId);

    boolean isAutoScaleLoadBalancer(Long loadBalancerId);
}

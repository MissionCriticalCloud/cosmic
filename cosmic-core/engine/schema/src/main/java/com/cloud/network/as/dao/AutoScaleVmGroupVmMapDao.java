package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScaleVmGroupVmMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AutoScaleVmGroupVmMapDao extends GenericDao<AutoScaleVmGroupVmMapVO, Long> {
    public Integer countByGroup(long vmGroupId);

    public List<AutoScaleVmGroupVmMapVO> listByGroup(long vmGroupId);

    public int remove(long vmGroupId, long vmId);
}

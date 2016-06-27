package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.InstanceGroupVMMapVO;

import java.util.List;

public interface InstanceGroupVMMapDao extends GenericDao<InstanceGroupVMMapVO, Long> {
    List<InstanceGroupVMMapVO> listByInstanceId(long instanceId);

    List<InstanceGroupVMMapVO> listByGroupId(long groupId);

    InstanceGroupVMMapVO findByVmIdGroupId(long instanceId, long groupId);
}

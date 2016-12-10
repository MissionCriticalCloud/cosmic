package com.cloud.api.query.dao;

import com.cloud.api.query.vo.InstanceGroupJoinVO;
import com.cloud.api.response.InstanceGroupResponse;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.InstanceGroup;

public interface InstanceGroupJoinDao extends GenericDao<InstanceGroupJoinVO, Long> {

    InstanceGroupResponse newInstanceGroupResponse(InstanceGroupJoinVO group);

    InstanceGroupJoinVO newInstanceGroupView(InstanceGroup group);
}

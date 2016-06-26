package com.cloud.api.query.dao;

import com.cloud.api.query.vo.InstanceGroupJoinVO;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.InstanceGroup;
import org.apache.cloudstack.api.response.InstanceGroupResponse;

public interface InstanceGroupJoinDao extends GenericDao<InstanceGroupJoinVO, Long> {

    InstanceGroupResponse newInstanceGroupResponse(InstanceGroupJoinVO group);

    InstanceGroupJoinVO newInstanceGroupView(InstanceGroup group);
}

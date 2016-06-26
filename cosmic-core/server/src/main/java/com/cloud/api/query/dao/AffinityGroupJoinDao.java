package com.cloud.api.query.dao;

import com.cloud.api.query.vo.AffinityGroupJoinVO;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.affinity.AffinityGroup;
import org.apache.cloudstack.affinity.AffinityGroupResponse;

import java.util.List;

public interface AffinityGroupJoinDao extends GenericDao<AffinityGroupJoinVO, Long> {

    AffinityGroupResponse newAffinityGroupResponse(AffinityGroupJoinVO vsg);

    AffinityGroupResponse setAffinityGroupResponse(AffinityGroupResponse vsgData, AffinityGroupJoinVO vsg);

    List<AffinityGroupJoinVO> newAffinityGroupView(AffinityGroup ag);

    List<AffinityGroupJoinVO> searchByIds(Long... ids);
}

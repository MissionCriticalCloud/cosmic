package com.cloud.api.query.dao;

import com.cloud.affinity.AffinityGroup;
import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.api.query.vo.AffinityGroupJoinVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AffinityGroupJoinDao extends GenericDao<AffinityGroupJoinVO, Long> {

    AffinityGroupResponse newAffinityGroupResponse(AffinityGroupJoinVO vsg);

    AffinityGroupResponse setAffinityGroupResponse(AffinityGroupResponse vsgData, AffinityGroupJoinVO vsg);

    List<AffinityGroupJoinVO> newAffinityGroupView(AffinityGroup ag);

    List<AffinityGroupJoinVO> searchByIds(Long... ids);
}

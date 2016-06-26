package com.cloud.api.query.dao;

import com.cloud.api.ApiResponseHelper;
import com.cloud.api.query.vo.InstanceGroupJoinVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.vm.InstanceGroup;
import org.apache.cloudstack.api.response.InstanceGroupResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InstanceGroupJoinDaoImpl extends GenericDaoBase<InstanceGroupJoinVO, Long> implements InstanceGroupJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(InstanceGroupJoinDaoImpl.class);

    private final SearchBuilder<InstanceGroupJoinVO> vrIdSearch;

    protected InstanceGroupJoinDaoImpl() {

        vrIdSearch = createSearchBuilder();
        vrIdSearch.and("id", vrIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        vrIdSearch.done();

        this._count = "select count(distinct id) from instance_group_view WHERE ";
    }

    @Override
    public InstanceGroupResponse newInstanceGroupResponse(final InstanceGroupJoinVO group) {
        final InstanceGroupResponse groupResponse = new InstanceGroupResponse();
        groupResponse.setId(group.getUuid());
        groupResponse.setName(group.getName());
        groupResponse.setCreated(group.getCreated());

        ApiResponseHelper.populateOwner(groupResponse, group);

        groupResponse.setObjectName("instancegroup");
        return groupResponse;
    }

    @Override
    public InstanceGroupJoinVO newInstanceGroupView(final InstanceGroup group) {
        final SearchCriteria<InstanceGroupJoinVO> sc = vrIdSearch.create();
        sc.setParameters("id", group.getId());
        final List<InstanceGroupJoinVO> grps = searchIncludingRemoved(sc, null, null, false);
        assert grps != null && grps.size() == 1 : "No vm group found for group id " + group.getId();
        return grps.get(0);
    }
}

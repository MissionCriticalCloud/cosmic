package com.cloud.api.query.dao;

import com.cloud.api.ApiResponseHelper;
import com.cloud.api.query.vo.AffinityGroupJoinVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.affinity.AffinityGroup;
import org.apache.cloudstack.affinity.AffinityGroupResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AffinityGroupJoinDaoImpl extends GenericDaoBase<AffinityGroupJoinVO, Long> implements AffinityGroupJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(AffinityGroupJoinDaoImpl.class);
    private final SearchBuilder<AffinityGroupJoinVO> agSearch;
    private final SearchBuilder<AffinityGroupJoinVO> agIdSearch;
    @Inject
    private ConfigurationDao _configDao;

    protected AffinityGroupJoinDaoImpl() {

        agSearch = createSearchBuilder();
        agSearch.and("idIN", agSearch.entity().getId(), SearchCriteria.Op.IN);
        agSearch.done();

        agIdSearch = createSearchBuilder();
        agIdSearch.and("id", agIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        agIdSearch.done();

        this._count = "select count(distinct id) from affinity_group_view WHERE ";
    }

    @Override
    public AffinityGroupResponse newAffinityGroupResponse(final AffinityGroupJoinVO vag) {
        final AffinityGroupResponse agResponse = new AffinityGroupResponse();
        agResponse.setId(vag.getUuid());
        agResponse.setName(vag.getName());
        agResponse.setDescription(vag.getDescription());
        agResponse.setType(vag.getType());

        ApiResponseHelper.populateOwner(agResponse, vag);

        // update vm information
        final long instanceId = vag.getVmId();
        if (instanceId > 0) {
            final List<String> vmIdList = new ArrayList<>();
            vmIdList.add(vag.getVmUuid());
            agResponse.setVMIdList(vmIdList);
        }

        agResponse.setObjectName("affinitygroup");
        return agResponse;
    }

    @Override
    public AffinityGroupResponse setAffinityGroupResponse(final AffinityGroupResponse vagData, final AffinityGroupJoinVO vag) {
        // update vm information
        final long instanceId = vag.getVmId();
        if (instanceId > 0) {
            vagData.addVMId(vag.getVmUuid());
        }
        return vagData;
    }

    @Override
    public List<AffinityGroupJoinVO> newAffinityGroupView(final AffinityGroup ag) {

        final SearchCriteria<AffinityGroupJoinVO> sc = agIdSearch.create();
        sc.setParameters("id", ag.getId());
        return searchIncludingRemoved(sc, null, null, false);
    }

    @Override
    public List<AffinityGroupJoinVO> searchByIds(final Long... agIds) {
        // set detail batch query size
        int DETAILS_BATCH_SIZE = 2000;
        final String batchCfg = _configDao.getValue("detail.batch.query.size");
        if (batchCfg != null) {
            DETAILS_BATCH_SIZE = Integer.parseInt(batchCfg);
        }
        // query details by batches
        final List<AffinityGroupJoinVO> uvList = new ArrayList<>();
        // query details by batches
        int curr_index = 0;
        if (agIds.length > DETAILS_BATCH_SIZE) {
            while ((curr_index + DETAILS_BATCH_SIZE) <= agIds.length) {
                final Long[] ids = new Long[DETAILS_BATCH_SIZE];
                for (int k = 0, j = curr_index; j < curr_index + DETAILS_BATCH_SIZE; j++, k++) {
                    ids[k] = agIds[j];
                }
                final SearchCriteria<AffinityGroupJoinVO> sc = agSearch.create();
                sc.setParameters("idIN", ids);
                final List<AffinityGroupJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
                if (vms != null) {
                    uvList.addAll(vms);
                }
                curr_index += DETAILS_BATCH_SIZE;
            }
        }
        if (curr_index < agIds.length) {
            final int batch_size = (agIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];
            for (int k = 0, j = curr_index; j < curr_index + batch_size; j++, k++) {
                ids[k] = agIds[j];
            }
            final SearchCriteria<AffinityGroupJoinVO> sc = agSearch.create();
            sc.setParameters("idIN", ids);
            final List<AffinityGroupJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
            if (vms != null) {
                uvList.addAll(vms);
            }
        }
        return uvList;
    }
}

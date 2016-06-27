package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScaleVmGroupVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AutoScaleVmGroupDaoImpl extends GenericDaoBase<AutoScaleVmGroupVO, Long> implements AutoScaleVmGroupDao {

    @Override
    public List<AutoScaleVmGroupVO> listByAll(final Long loadBalancerId, final Long profileId) {
        final SearchCriteria<AutoScaleVmGroupVO> sc = createSearchCriteria();

        if (loadBalancerId != null) {
            sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        }

        if (profileId != null) {
            sc.addAnd("profileId", SearchCriteria.Op.EQ, profileId);
        }

        return listBy(sc);
    }

    @Override
    public boolean isProfileInUse(final long profileId) {
        final SearchCriteria<AutoScaleVmGroupVO> sc = createSearchCriteria();
        sc.addAnd("profileId", SearchCriteria.Op.EQ, profileId);
        return findOneBy(sc) != null;
    }

    @Override
    public boolean isAutoScaleLoadBalancer(final Long loadBalancerId) {
        final GenericSearchBuilder<AutoScaleVmGroupVO, Long> CountByAccount = createSearchBuilder(Long.class);
        CountByAccount.select(null, Func.COUNT, null);
        CountByAccount.and("loadBalancerId", CountByAccount.entity().getLoadBalancerId(), SearchCriteria.Op.EQ);

        final SearchCriteria<Long> sc = CountByAccount.create();
        sc.setParameters("loadBalancerId", loadBalancerId);
        return customSearch(sc, null).get(0) > 0;
    }
}

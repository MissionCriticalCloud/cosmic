package org.apache.cloudstack.region.gslb;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class GlobalLoadBalancerLbRuleMapDaoImpl extends GenericDaoBase<GlobalLoadBalancerLbRuleMapVO, Long> implements GlobalLoadBalancerLbRuleMapDao {

    private final SearchBuilder<GlobalLoadBalancerLbRuleMapVO> listByGslbRuleId;
    private final SearchBuilder<GlobalLoadBalancerLbRuleMapVO> listByLbGslbRuleId;

    public GlobalLoadBalancerLbRuleMapDaoImpl() {
        listByGslbRuleId = createSearchBuilder();
        listByGslbRuleId.and("gslbLoadBalancerId", listByGslbRuleId.entity().getGslbLoadBalancerId(), SearchCriteria.Op.EQ);
        listByGslbRuleId.done();

        listByLbGslbRuleId = createSearchBuilder();
        listByLbGslbRuleId.and("gslbLoadBalancerId", listByLbGslbRuleId.entity().getGslbLoadBalancerId(), SearchCriteria.Op.EQ);
        listByLbGslbRuleId.and("loadBalancerId", listByLbGslbRuleId.entity().getLoadBalancerId(), SearchCriteria.Op.EQ);
        listByLbGslbRuleId.done();
    }

    @Override
    public List<GlobalLoadBalancerLbRuleMapVO> listByGslbRuleId(final long gslbRuleId) {
        final SearchCriteria<GlobalLoadBalancerLbRuleMapVO> sc = listByGslbRuleId.create();
        sc.setParameters("gslbLoadBalancerId", gslbRuleId);
        return listBy(sc);
    }

    @Override
    public GlobalLoadBalancerLbRuleMapVO findByGslbRuleIdAndLbRuleId(final long gslbRuleId, final long lbRuleId) {
        final SearchCriteria<GlobalLoadBalancerLbRuleMapVO> sc = listByLbGslbRuleId.create();
        sc.setParameters("gslbLoadBalancerId", gslbRuleId);
        sc.setParameters("loadBalancerId", lbRuleId);
        return findOneBy(sc);
    }
}

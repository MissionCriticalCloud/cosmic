package org.apache.cloudstack.region.gslb;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class GlobalLoadBalancerDaoImpl extends GenericDaoBase<GlobalLoadBalancerRuleVO, Long> implements GlobalLoadBalancerRuleDao {

    private final SearchBuilder<GlobalLoadBalancerRuleVO> listByDomainSearch;
    private final SearchBuilder<GlobalLoadBalancerRuleVO> listByRegionIDSearch;
    private final SearchBuilder<GlobalLoadBalancerRuleVO> AccountIdSearch;

    public GlobalLoadBalancerDaoImpl() {
        listByDomainSearch = createSearchBuilder();
        listByDomainSearch.and("gslbDomain", listByDomainSearch.entity().getGslbDomain(), SearchCriteria.Op.EQ);
        listByDomainSearch.done();

        AccountIdSearch = createSearchBuilder();
        AccountIdSearch.and("account", AccountIdSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountIdSearch.done();

        listByRegionIDSearch = createSearchBuilder();
        listByRegionIDSearch.and("region", listByRegionIDSearch.entity().getRegion(), SearchCriteria.Op.EQ);
        listByRegionIDSearch.done();
    }

    @Override
    public List<GlobalLoadBalancerRuleVO> listByRegionId(final int regionId) {
        final SearchCriteria<GlobalLoadBalancerRuleVO> sc = listByRegionIDSearch.create();
        sc.setParameters("region", regionId);
        return listBy(sc);
    }

    @Override
    public List<GlobalLoadBalancerRuleVO> listByAccount(final long accountId) {
        final SearchCriteria<GlobalLoadBalancerRuleVO> sc = AccountIdSearch.create();
        sc.setParameters("account", accountId);
        return listBy(sc, null);
    }

    @Override
    public GlobalLoadBalancerRuleVO findByDomainName(final String domainName) {
        final SearchCriteria<GlobalLoadBalancerRuleVO> sc = listByDomainSearch.create();
        sc.setParameters("gslbDomain", domainName);
        return findOneBy(sc);
    }
}

package com.cloud.network.dao;

import com.cloud.network.rules.FirewallRule.State;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import javax.inject.Inject;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LoadBalancerDaoImpl extends GenericDaoBase<LoadBalancerVO, Long> implements LoadBalancerDao {
    protected final SearchBuilder<LoadBalancerVO> TransitionStateSearch;
    private final SearchBuilder<LoadBalancerVO> ListByIp;
    @Inject
    protected FirewallRulesCidrsDao _portForwardingRulesCidrsDao;

    protected LoadBalancerDaoImpl() {
        ListByIp = createSearchBuilder();
        ListByIp.and("ipAddressId", ListByIp.entity().getSourceIpAddressId(), SearchCriteria.Op.EQ);
        ListByIp.and("networkId", ListByIp.entity().getNetworkId(), SearchCriteria.Op.EQ);
        ListByIp.and("scheme", ListByIp.entity().getScheme(), SearchCriteria.Op.EQ);
        ListByIp.done();

        TransitionStateSearch = createSearchBuilder();
        TransitionStateSearch.and("networkId", TransitionStateSearch.entity().getNetworkId(), Op.EQ);
        TransitionStateSearch.and("state", TransitionStateSearch.entity().getState(), Op.IN);
        TransitionStateSearch.and("scheme", TransitionStateSearch.entity().getScheme(), Op.EQ);
        TransitionStateSearch.done();
    }

    @Override
    public List<LoadBalancerVO> listByIpAddress(final long ipAddressId) {
        final SearchCriteria<LoadBalancerVO> sc = ListByIp.create();
        sc.setParameters("ipAddressId", ipAddressId);
        return listBy(sc);
    }

    @Override
    public List<LoadBalancerVO> listByNetworkIdAndScheme(final long networkId, final Scheme scheme) {
        final SearchCriteria<LoadBalancerVO> sc = ListByIp.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("scheme", scheme);
        return listBy(sc);
    }

    @Override
    public List<LoadBalancerVO> listInTransitionStateByNetworkIdAndScheme(final long networkId, final Scheme scheme) {
        final SearchCriteria<LoadBalancerVO> sc = TransitionStateSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("state", State.Add.toString(), State.Revoke.toString());
        sc.setParameters("scheme", scheme);
        return listBy(sc);
    }
}

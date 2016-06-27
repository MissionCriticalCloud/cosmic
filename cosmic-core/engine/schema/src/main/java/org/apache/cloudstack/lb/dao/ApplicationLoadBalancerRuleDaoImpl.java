package org.apache.cloudstack.lb.dao;

import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.State;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.net.Ip;
import org.apache.cloudstack.lb.ApplicationLoadBalancerRuleVO;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ApplicationLoadBalancerRuleDaoImpl extends GenericDaoBase<ApplicationLoadBalancerRuleVO, Long> implements ApplicationLoadBalancerRuleDao {
    protected final SearchBuilder<ApplicationLoadBalancerRuleVO> AllFieldsSearch;
    protected final SearchBuilder<ApplicationLoadBalancerRuleVO> NotRevokedSearch;
    final GenericSearchBuilder<ApplicationLoadBalancerRuleVO, String> listIps;
    final GenericSearchBuilder<ApplicationLoadBalancerRuleVO, Long> CountBy;
    final GenericSearchBuilder<ApplicationLoadBalancerRuleVO, Long> CountNotRevoked;
    final GenericSearchBuilder<ApplicationLoadBalancerRuleVO, Long> CountActive;

    protected ApplicationLoadBalancerRuleDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("sourceIp", AllFieldsSearch.entity().getSourceIp(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("sourceIpNetworkId", AllFieldsSearch.entity().getSourceIpNetworkId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("scheme", AllFieldsSearch.entity().getScheme(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        listIps = createSearchBuilder(String.class);
        listIps.select(null, Func.DISTINCT, listIps.entity().getSourceIp());
        listIps.and("sourceIpNetworkId", listIps.entity().getSourceIpNetworkId(), Op.EQ);
        listIps.and("scheme", listIps.entity().getScheme(), Op.EQ);
        listIps.done();

        CountBy = createSearchBuilder(Long.class);
        CountBy.select(null, Func.COUNT, CountBy.entity().getId());
        CountBy.and("sourceIp", CountBy.entity().getSourceIp(), Op.EQ);
        CountBy.and("sourceIpNetworkId", CountBy.entity().getSourceIpNetworkId(), Op.EQ);
        CountBy.done();

        NotRevokedSearch = createSearchBuilder();
        NotRevokedSearch.and("sourceIp", NotRevokedSearch.entity().getSourceIp(), SearchCriteria.Op.EQ);
        NotRevokedSearch.and("sourceIpNetworkId", NotRevokedSearch.entity().getSourceIpNetworkId(), SearchCriteria.Op.EQ);
        NotRevokedSearch.and("state", NotRevokedSearch.entity().getState(), SearchCriteria.Op.NEQ);
        NotRevokedSearch.done();

        CountNotRevoked = createSearchBuilder(Long.class);
        CountNotRevoked.select(null, Func.COUNT, CountNotRevoked.entity().getId());
        CountNotRevoked.and("sourceIp", CountNotRevoked.entity().getSourceIp(), Op.EQ);
        CountNotRevoked.and("state", CountNotRevoked.entity().getState(), Op.NEQ);
        CountNotRevoked.and("sourceIpNetworkId", CountNotRevoked.entity().getSourceIpNetworkId(), Op.EQ);
        CountNotRevoked.done();

        CountActive = createSearchBuilder(Long.class);
        CountActive.select(null, Func.COUNT, CountActive.entity().getId());
        CountActive.and("sourceIp", CountActive.entity().getSourceIp(), Op.EQ);
        CountActive.and("state", CountActive.entity().getState(), Op.EQ);
        CountActive.and("sourceIpNetworkId", CountActive.entity().getSourceIpNetworkId(), Op.EQ);
        CountActive.done();
    }

    @Override
    public List<ApplicationLoadBalancerRuleVO> listBySrcIpSrcNtwkId(final Ip sourceIp, final long sourceNetworkId) {
        final SearchCriteria<ApplicationLoadBalancerRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("sourceIp", sourceIp);
        sc.setParameters("sourceIpNetworkId", sourceNetworkId);
        return listBy(sc);
    }

    @Override
    public List<String> listLbIpsBySourceIpNetworkId(final long sourceIpNetworkId) {
        final SearchCriteria<String> sc = listIps.create();
        sc.setParameters("sourceIpNetworkId", sourceIpNetworkId);
        return customSearch(sc, null);
    }

    @Override
    public long countBySourceIp(final Ip sourceIp, final long sourceIpNetworkId) {
        final SearchCriteria<Long> sc = CountBy.create();
        sc.setParameters("sourceIp", sourceIp);
        sc.setParameters("sourceIpNetworkId", sourceIpNetworkId);
        final List<Long> results = customSearch(sc, null);
        return results.get(0);
    }

    @Override
    public List<ApplicationLoadBalancerRuleVO> listBySourceIpAndNotRevoked(final Ip sourceIp, final long sourceNetworkId) {
        final SearchCriteria<ApplicationLoadBalancerRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("sourceIp", sourceIp);
        sc.setParameters("sourceIpNetworkId", sourceNetworkId);
        sc.setParameters("state", FirewallRule.State.Revoke);
        return listBy(sc);
    }

    @Override
    public List<String> listLbIpsBySourceIpNetworkIdAndScheme(final long sourceIpNetworkId, final Scheme scheme) {
        final SearchCriteria<String> sc = listIps.create();
        sc.setParameters("sourceIpNetworkId", sourceIpNetworkId);
        sc.setParameters("scheme", scheme);
        return customSearch(sc, null);
    }

    @Override
    public long countBySourceIpAndNotRevoked(final Ip sourceIp, final long sourceIpNetworkId) {
        final SearchCriteria<Long> sc = CountNotRevoked.create();
        sc.setParameters("sourceIp", sourceIp);
        sc.setParameters("sourceIpNetworkId", sourceIpNetworkId);
        sc.setParameters("state", State.Revoke);
        final List<Long> results = customSearch(sc, null);
        return results.get(0);
    }

    @Override
    public long countActiveBySourceIp(final Ip sourceIp, final long sourceIpNetworkId) {
        final SearchCriteria<Long> sc = CountActive.create();
        sc.setParameters("sourceIp", sourceIp);
        sc.setParameters("sourceIpNetworkId", sourceIpNetworkId);
        sc.setParameters("state", State.Active);
        final List<Long> results = customSearch(sc, null);
        return results.get(0);
    }
}

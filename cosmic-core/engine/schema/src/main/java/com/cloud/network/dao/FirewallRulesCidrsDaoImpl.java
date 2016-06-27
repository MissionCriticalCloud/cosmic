package com.cloud.network.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FirewallRulesCidrsDaoImpl extends GenericDaoBase<FirewallRulesCidrsVO, Long> implements FirewallRulesCidrsDao {
    private static final Logger s_logger = LoggerFactory.getLogger(FirewallRulesCidrsDaoImpl.class);
    protected final SearchBuilder<FirewallRulesCidrsVO> CidrsSearch;

    protected FirewallRulesCidrsDaoImpl() {
        CidrsSearch = createSearchBuilder();
        CidrsSearch.and("firewallRuleId", CidrsSearch.entity().getFirewallRuleId(), SearchCriteria.Op.EQ);
        CidrsSearch.done();
    }

    @Override
    @DB
    public void persist(final long firewallRuleId, final List<String> sourceCidrs) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        for (final String tag : sourceCidrs) {
            final FirewallRulesCidrsVO vo = new FirewallRulesCidrsVO(firewallRuleId, tag);
            persist(vo);
        }
        txn.commit();
    }

    @Override
    @DB
    public List<String> getSourceCidrs(final long firewallRuleId) {
        final SearchCriteria<FirewallRulesCidrsVO> sc = CidrsSearch.create();
        sc.setParameters("firewallRuleId", firewallRuleId);

        final List<FirewallRulesCidrsVO> results = search(sc, null);
        final List<String> cidrs = new ArrayList<>(results.size());
        for (final FirewallRulesCidrsVO result : results) {
            cidrs.add(result.getCidr());
        }

        return cidrs;
    }

    @Override
    @DB
    public List<FirewallRulesCidrsVO> listByFirewallRuleId(final long firewallRuleId) {
        final SearchCriteria<FirewallRulesCidrsVO> sc = CidrsSearch.create();
        sc.setParameters("firewallRuleId", firewallRuleId);

        final List<FirewallRulesCidrsVO> results = search(sc, null);

        return results;
    }
}

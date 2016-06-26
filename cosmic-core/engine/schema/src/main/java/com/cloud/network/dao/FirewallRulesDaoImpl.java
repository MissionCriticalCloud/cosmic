package com.cloud.network.dao;

import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.FirewallRuleType;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRule.State;
import com.cloud.network.rules.FirewallRule.TrafficType;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB
public class FirewallRulesDaoImpl extends GenericDaoBase<FirewallRuleVO, Long> implements FirewallRulesDao {

    protected final SearchBuilder<FirewallRuleVO> AllFieldsSearch;
    protected final SearchBuilder<FirewallRuleVO> NotRevokedSearch;
    protected final SearchBuilder<FirewallRuleVO> ReleaseSearch;
    protected final SearchBuilder<FirewallRuleVO> SystemRuleSearch;
    protected final GenericSearchBuilder<FirewallRuleVO, Long> RulesByIpCount;
    protected SearchBuilder<FirewallRuleVO> VmSearch;
    @Inject
    protected FirewallRulesCidrsDao _firewallRulesCidrsDao;
    @Inject
    ResourceTagDao _tagsDao;
    @Inject
    IPAddressDao _ipDao;

    protected FirewallRulesDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("ipId", AllFieldsSearch.entity().getSourceIpAddressId(), Op.EQ);
        AllFieldsSearch.and("protocol", AllFieldsSearch.entity().getProtocol(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("purpose", AllFieldsSearch.entity().getPurpose(), Op.EQ);
        AllFieldsSearch.and("account", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.and("domain", AllFieldsSearch.entity().getDomainId(), Op.EQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.and("related", AllFieldsSearch.entity().getRelated(), Op.EQ);
        AllFieldsSearch.and("trafficType", AllFieldsSearch.entity().getTrafficType(), Op.EQ);
        AllFieldsSearch.done();

        NotRevokedSearch = createSearchBuilder();
        NotRevokedSearch.and("ipId", NotRevokedSearch.entity().getSourceIpAddressId(), Op.EQ);
        NotRevokedSearch.and("state", NotRevokedSearch.entity().getState(), Op.NEQ);
        NotRevokedSearch.and("purpose", NotRevokedSearch.entity().getPurpose(), Op.EQ);
        NotRevokedSearch.and("protocol", NotRevokedSearch.entity().getProtocol(), Op.EQ);
        NotRevokedSearch.and("sourcePortStart", NotRevokedSearch.entity().getSourcePortStart(), Op.EQ);
        NotRevokedSearch.and("sourcePortEnd", NotRevokedSearch.entity().getSourcePortEnd(), Op.EQ);
        NotRevokedSearch.and("networkId", NotRevokedSearch.entity().getNetworkId(), Op.EQ);
        NotRevokedSearch.and("trafficType", NotRevokedSearch.entity().getTrafficType(), Op.EQ);
        NotRevokedSearch.done();

        ReleaseSearch = createSearchBuilder();
        ReleaseSearch.and("protocol", ReleaseSearch.entity().getProtocol(), Op.EQ);
        ReleaseSearch.and("ipId", ReleaseSearch.entity().getSourceIpAddressId(), Op.EQ);
        ReleaseSearch.and("purpose", ReleaseSearch.entity().getPurpose(), Op.EQ);
        ReleaseSearch.and("ports", ReleaseSearch.entity().getSourcePortStart(), Op.IN);
        ReleaseSearch.done();

        SystemRuleSearch = createSearchBuilder();
        SystemRuleSearch.and("type", SystemRuleSearch.entity().getType(), Op.EQ);
        SystemRuleSearch.and("ipId", SystemRuleSearch.entity().getSourceIpAddressId(), Op.NULL);
        SystemRuleSearch.done();

        RulesByIpCount = createSearchBuilder(Long.class);
        RulesByIpCount.select(null, Func.COUNT, RulesByIpCount.entity().getId());
        RulesByIpCount.and("ipAddressId", RulesByIpCount.entity().getSourceIpAddressId(), Op.EQ);
        RulesByIpCount.and("state", RulesByIpCount.entity().getState(), Op.EQ);
        RulesByIpCount.done();
    }

    @Override
    public List<FirewallRuleVO> listByIpAndPurposeAndNotRevoked(final long ipId, final FirewallRule.Purpose purpose) {
        final SearchCriteria<FirewallRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("ipId", ipId);
        sc.setParameters("state", State.Revoke);

        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }

        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByNetworkAndPurposeAndNotRevoked(final long networkId, final FirewallRule.Purpose purpose) {
        final SearchCriteria<FirewallRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("state", State.Revoke);

        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }

        return listBy(sc);
    }

    @Override
    public boolean setStateToAdd(final FirewallRuleVO rule) {
        final SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", rule.getId());
        sc.setParameters("state", State.Staged);

        rule.setState(State.Add);

        return update(rule, sc) > 0;
    }

    @Override
    public boolean revoke(final FirewallRuleVO rule) {
        rule.setState(State.Revoke);
        return update(rule.getId(), rule);
    }

    @Override
    public boolean releasePorts(final long ipId, final String protocol, final FirewallRule.Purpose purpose, final int[] ports) {
        final SearchCriteria<FirewallRuleVO> sc = ReleaseSearch.create();
        sc.setParameters("protocol", protocol);
        sc.setParameters("ipId", ipId);
        sc.setParameters("purpose", purpose);
        sc.setParameters("ports", ports);

        final int results = remove(sc);
        return results == ports.length;
    }

    @Override
    public List<FirewallRuleVO> listByIpAndPurpose(final long ipId, final FirewallRule.Purpose purpose) {
        final SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipId", ipId);
        sc.setParameters("purpose", purpose);

        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByNetworkAndPurpose(final long networkId, final FirewallRule.Purpose purpose) {
        final SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("purpose", purpose);
        sc.setParameters("networkId", networkId);

        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listStaticNatByVmId(final long vmId) {
        if (VmSearch == null) {
            final SearchBuilder<IPAddressVO> IpSearch = _ipDao.createSearchBuilder();
            IpSearch.and("associatedWithVmId", IpSearch.entity().getAssociatedWithVmId(), SearchCriteria.Op.EQ);
            IpSearch.and("oneToOneNat", IpSearch.entity().isOneToOneNat(), SearchCriteria.Op.NNULL);

            VmSearch = createSearchBuilder();
            VmSearch.and("purpose", VmSearch.entity().getPurpose(), Op.EQ);
            VmSearch.join("ipSearch", IpSearch, VmSearch.entity().getSourceIpAddressId(), IpSearch.entity().getId(), JoinBuilder.JoinType.INNER);
            VmSearch.done();
        }

        final SearchCriteria<FirewallRuleVO> sc = VmSearch.create();
        sc.setParameters("purpose", Purpose.StaticNat);
        sc.setJoinParameters("ipSearch", "associatedWithVmId", vmId);

        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByIpPurposeAndProtocolAndNotRevoked(final long ipAddressId, final Integer startPort, final Integer endPort, final String protocol,
                                                                        final FirewallRule.Purpose purpose) {
        final SearchCriteria<FirewallRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("ipId", ipAddressId);
        sc.setParameters("state", State.Revoke);

        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }

        if (protocol != null) {
            sc.setParameters("protocol", protocol);
        }

        sc.setParameters("sourcePortStart", startPort);

        sc.setParameters("sourcePortEnd", endPort);

        return listBy(sc);
    }

    @Override
    public FirewallRuleVO findByRelatedId(final long ruleId) {
        final SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("related", ruleId);
        sc.setParameters("purpose", Purpose.Firewall);

        return findOneBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listSystemRules() {
        final SearchCriteria<FirewallRuleVO> sc = SystemRuleSearch.create();
        sc.setParameters("type", FirewallRuleType.System.toString());
        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByIp(final long ipId) {
        final SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipId", ipId);

        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByIpAndNotRevoked(final long ipId) {
        final SearchCriteria<FirewallRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("ipId", ipId);
        sc.setParameters("state", State.Revoke);

        return listBy(sc);
    }

    @Override
    public long countRulesByIpId(final long sourceIpId) {
        final SearchCriteria<Long> sc = RulesByIpCount.create();
        sc.setParameters("ipAddressId", sourceIpId);
        return customSearch(sc, null).get(0);
    }

    @Override
    public long countRulesByIpIdAndState(final long sourceIpId, final FirewallRule.State state) {
        final SearchCriteria<Long> sc = RulesByIpCount.create();
        sc.setParameters("ipAddressId", sourceIpId);
        if (state != null) {
            sc.setParameters("state", state);
        }
        return customSearch(sc, null).get(0);
    }

    @Override
    public List<FirewallRuleVO> listByNetworkPurposeTrafficTypeAndNotRevoked(final long networkId, final FirewallRule.Purpose purpose, final TrafficType trafficType) {
        final SearchCriteria<FirewallRuleVO> sc = NotRevokedSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("state", State.Revoke);
        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }
        sc.setParameters("trafficType", trafficType);

        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByNetworkPurposeTrafficType(final long networkId, final Purpose purpose, final TrafficType trafficType) {
        final SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);

        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }

        sc.setParameters("trafficType", trafficType);

        return listBy(sc);
    }

    @Override
    public List<FirewallRuleVO> listByIpAndPurposeWithState(final Long ipId, final Purpose purpose, final State state) {
        final SearchCriteria<FirewallRuleVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipId", ipId);

        if (state != null) {
            sc.setParameters("state", state);
        }

        if (purpose != null) {
            sc.setParameters("purpose", purpose);
        }

        return listBy(sc);
    }

    @Override
    public void loadSourceCidrs(final FirewallRuleVO rule) {
        final List<String> sourceCidrs = _firewallRulesCidrsDao.getSourceCidrs(rule.getId());
        rule.setSourceCidrList(sourceCidrs);
    }

    @Override
    @DB
    public FirewallRuleVO persist(final FirewallRuleVO firewallRule) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final FirewallRuleVO dbfirewallRule = super.persist(firewallRule);
        saveSourceCidrs(firewallRule, firewallRule.getSourceCidrList());
        loadSourceCidrs(dbfirewallRule);

        txn.commit();
        return dbfirewallRule;
    }

    public void saveSourceCidrs(final FirewallRuleVO firewallRule, final List<String> cidrList) {
        if (cidrList == null) {
            return;
        }
        _firewallRulesCidrsDao.persist(firewallRule.getId(), cidrList);
    }

    @Override
    @DB
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final FirewallRuleVO entry = findById(id);
        if (entry != null) {
            if (entry.getPurpose() == Purpose.LoadBalancing) {
                _tagsDao.removeByIdAndType(id, ResourceObjectType.LoadBalancer);
            } else if (entry.getPurpose() == Purpose.PortForwarding) {
                _tagsDao.removeByIdAndType(id, ResourceObjectType.PortForwardingRule);
            } else if (entry.getPurpose() == Purpose.Firewall) {
                _tagsDao.removeByIdAndType(id, ResourceObjectType.FirewallRule);
            } else if (entry.getPurpose() == Purpose.NetworkACL) {
                _tagsDao.removeByIdAndType(id, ResourceObjectType.NetworkACL);
            }
        }
        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}

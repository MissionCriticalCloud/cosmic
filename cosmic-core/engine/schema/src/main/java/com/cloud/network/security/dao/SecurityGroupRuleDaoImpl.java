package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupRuleVO;
import com.cloud.network.security.SecurityGroupVO;
import com.cloud.network.security.SecurityRule.SecurityRuleType;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SecurityGroupRuleDaoImpl extends GenericDaoBase<SecurityGroupRuleVO, Long> implements SecurityGroupRuleDao {

    protected SearchBuilder<SecurityGroupRuleVO> securityGroupIdSearch;
    protected SearchBuilder<SecurityGroupRuleVO> securityGroupIdAndTypeSearch;
    protected SearchBuilder<SecurityGroupRuleVO> allowedSecurityGroupIdSearch;
    protected SearchBuilder<SecurityGroupRuleVO> protoPortsAndCidrSearch;
    protected SearchBuilder<SecurityGroupRuleVO> protoPortsAndSecurityGroupNameSearch;
    protected SearchBuilder<SecurityGroupRuleVO> protoPortsAndSecurityGroupIdSearch;
    @Inject
    SecurityGroupDao _securityGroupDao;
    @Inject
    ResourceTagDao _tagsDao;

    protected SecurityGroupRuleDaoImpl() {
        securityGroupIdSearch = createSearchBuilder();
        securityGroupIdSearch.and("securityGroupId", securityGroupIdSearch.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        securityGroupIdSearch.done();

        securityGroupIdAndTypeSearch = createSearchBuilder();
        securityGroupIdAndTypeSearch.and("securityGroupId", securityGroupIdAndTypeSearch.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        securityGroupIdAndTypeSearch.and("type", securityGroupIdAndTypeSearch.entity().getType(), SearchCriteria.Op.EQ);
        securityGroupIdAndTypeSearch.done();

        allowedSecurityGroupIdSearch = createSearchBuilder();
        allowedSecurityGroupIdSearch.and("allowedNetworkId", allowedSecurityGroupIdSearch.entity().getAllowedNetworkId(), SearchCriteria.Op.EQ);
        allowedSecurityGroupIdSearch.done();

        protoPortsAndCidrSearch = createSearchBuilder();
        protoPortsAndCidrSearch.and("securityGroupId", protoPortsAndCidrSearch.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        protoPortsAndCidrSearch.and("proto", protoPortsAndCidrSearch.entity().getProtocol(), SearchCriteria.Op.EQ);
        protoPortsAndCidrSearch.and("startPort", protoPortsAndCidrSearch.entity().getStartPort(), SearchCriteria.Op.EQ);
        protoPortsAndCidrSearch.and("endPort", protoPortsAndCidrSearch.entity().getEndPort(), SearchCriteria.Op.EQ);
        protoPortsAndCidrSearch.and("cidr", protoPortsAndCidrSearch.entity().getAllowedSourceIpCidr(), SearchCriteria.Op.EQ);
        protoPortsAndCidrSearch.done();

        protoPortsAndSecurityGroupIdSearch = createSearchBuilder();
        protoPortsAndSecurityGroupIdSearch.and("securityGroupId", protoPortsAndSecurityGroupIdSearch.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        protoPortsAndSecurityGroupIdSearch.and("proto", protoPortsAndSecurityGroupIdSearch.entity().getProtocol(), SearchCriteria.Op.EQ);
        protoPortsAndSecurityGroupIdSearch.and("startPort", protoPortsAndSecurityGroupIdSearch.entity().getStartPort(), SearchCriteria.Op.EQ);
        protoPortsAndSecurityGroupIdSearch.and("endPort", protoPortsAndSecurityGroupIdSearch.entity().getEndPort(), SearchCriteria.Op.EQ);
        protoPortsAndSecurityGroupIdSearch.and("allowedNetworkId", protoPortsAndSecurityGroupIdSearch.entity().getAllowedNetworkId(), SearchCriteria.Op.EQ);
    }

    @Override
    public List<SecurityGroupRuleVO> listBySecurityGroupId(final long securityGroupId, final SecurityRuleType type) {
        final SearchCriteria<SecurityGroupRuleVO> sc = securityGroupIdAndTypeSearch.create();
        sc.setParameters("securityGroupId", securityGroupId);
        final String dbType;
        if (type == SecurityRuleType.EgressRule) {
            dbType = SecurityRuleType.EgressRule.getType();
        } else {
            dbType = SecurityRuleType.IngressRule.getType();
        }

        sc.setParameters("type", dbType);
        return listBy(sc);
    }

    @Override
    public List<SecurityGroupRuleVO> listByAllowedSecurityGroupId(final long securityGroupId) {
        final SearchCriteria<SecurityGroupRuleVO> sc = allowedSecurityGroupIdSearch.create();
        sc.setParameters("allowedNetworkId", securityGroupId);
        return listBy(sc);
    }

    @Override
    public SecurityGroupRuleVO findByProtoPortsAndCidr(final long securityGroupId, final String proto, final int startPort, final int endPort, final String cidr) {
        final SearchCriteria<SecurityGroupRuleVO> sc = protoPortsAndCidrSearch.create();
        sc.setParameters("securityGroupId", securityGroupId);
        sc.setParameters("proto", proto);
        sc.setParameters("startPort", startPort);
        sc.setParameters("endPort", endPort);
        sc.setParameters("cidr", cidr);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public SecurityGroupRuleVO findByProtoPortsAndGroup(final String proto, final int startPort, final int endPort, final String securityGroup) {
        final SearchCriteria<SecurityGroupRuleVO> sc = protoPortsAndSecurityGroupNameSearch.create();
        sc.setParameters("proto", proto);
        sc.setParameters("startPort", startPort);
        sc.setParameters("endPort", endPort);
        sc.setJoinParameters("groupName", "groupName", securityGroup);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public SecurityGroupRuleVO findByProtoPortsAndAllowedGroupId(final long securityGroupId, final String proto, final int startPort, final int endPort, final Long
            allowedGroupId) {
        final SearchCriteria<SecurityGroupRuleVO> sc = protoPortsAndSecurityGroupIdSearch.create();
        sc.addAnd("securityGroupId", SearchCriteria.Op.EQ, securityGroupId);
        sc.setParameters("proto", proto);
        sc.setParameters("startPort", startPort);
        sc.setParameters("endPort", endPort);
        sc.setParameters("allowedNetworkId", allowedGroupId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public int deleteBySecurityGroup(final long securityGroupId) {
        final SearchCriteria<SecurityGroupRuleVO> sc = securityGroupIdSearch.create();
        sc.setParameters("securityGroupId", securityGroupId);
        return expunge(sc);
    }

    @Override
    public int deleteByPortProtoAndGroup(final long securityGroupId, final String protocol, final int startPort, final int endPort, final Long allowedGroupId) {
        final SearchCriteria<SecurityGroupRuleVO> sc = protoPortsAndSecurityGroupIdSearch.create();
        sc.setParameters("securityGroupId", securityGroupId);
        sc.setParameters("proto", protocol);
        sc.setParameters("startPort", startPort);
        sc.setParameters("endPort", endPort);
        sc.setParameters("allowedNetworkId", allowedGroupId);
        return expunge(sc);
    }

    @Override
    public int deleteByPortProtoAndCidr(final long securityGroupId, final String protocol, final int startPort, final int endPort, final String cidr) {
        final SearchCriteria<SecurityGroupRuleVO> sc = protoPortsAndCidrSearch.create();
        sc.setParameters("securityGroupId", securityGroupId);
        sc.setParameters("proto", protocol);
        sc.setParameters("startPort", startPort);
        sc.setParameters("endPort", endPort);
        sc.setParameters("cidr", cidr);
        return expunge(sc);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        protoPortsAndSecurityGroupNameSearch = createSearchBuilder();
        protoPortsAndSecurityGroupNameSearch.and("proto", protoPortsAndSecurityGroupNameSearch.entity().getProtocol(), SearchCriteria.Op.EQ);
        protoPortsAndSecurityGroupNameSearch.and("startPort", protoPortsAndSecurityGroupNameSearch.entity().getStartPort(), SearchCriteria.Op.EQ);
        protoPortsAndSecurityGroupNameSearch.and("endPort", protoPortsAndSecurityGroupNameSearch.entity().getEndPort(), SearchCriteria.Op.EQ);
        final SearchBuilder<SecurityGroupVO> ngSb = _securityGroupDao.createSearchBuilder();
        ngSb.and("groupName", ngSb.entity().getName(), SearchCriteria.Op.EQ);
        protoPortsAndSecurityGroupNameSearch.join("groupName", ngSb, protoPortsAndSecurityGroupNameSearch.entity().getAllowedNetworkId(), ngSb.entity().getId(),
                JoinBuilder.JoinType.INNER);
        protoPortsAndSecurityGroupNameSearch.done();
        return super.configure(name, params);
    }

    @Override
    @DB
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SecurityGroupRuleVO entry = findById(id);
        if (entry != null) {
            _tagsDao.removeByIdAndType(id, ResourceObjectType.SecurityGroupRule);
        }
        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}

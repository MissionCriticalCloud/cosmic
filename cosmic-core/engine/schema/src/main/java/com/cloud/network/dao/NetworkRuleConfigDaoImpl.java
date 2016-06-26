package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NetworkRuleConfigDaoImpl extends GenericDaoBase<NetworkRuleConfigVO, Long> implements NetworkRuleConfigDao {
    protected SearchBuilder<NetworkRuleConfigVO> SecurityGroupIdSearch;

    protected NetworkRuleConfigDaoImpl() {
        SecurityGroupIdSearch = createSearchBuilder();
        SecurityGroupIdSearch.and("securityGroupId", SecurityGroupIdSearch.entity().getSecurityGroupId(), SearchCriteria.Op.EQ);
        SecurityGroupIdSearch.done();
    }

    @Override
    public List<NetworkRuleConfigVO> listBySecurityGroupId(final long securityGroupId) {
        final SearchCriteria<NetworkRuleConfigVO> sc = SecurityGroupIdSearch.create();
        sc.setParameters("securityGroupId", securityGroupId);
        return listBy(sc);
    }

    @Override
    public void deleteBySecurityGroup(final long securityGroupId) {
        final SearchCriteria<NetworkRuleConfigVO> sc = SecurityGroupIdSearch.create();
        sc.setParameters("securityGroupId", securityGroupId);
        expunge(sc);
    }
}

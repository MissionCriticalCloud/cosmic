package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupRulesVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SecurityGroupRulesDaoImpl extends GenericDaoBase<SecurityGroupRulesVO, Long> implements SecurityGroupRulesDao {
    private final SearchBuilder<SecurityGroupRulesVO> AccountGroupNameSearch;
    private final SearchBuilder<SecurityGroupRulesVO> AccountSearch;
    private final SearchBuilder<SecurityGroupRulesVO> GroupSearch;

    protected SecurityGroupRulesDaoImpl() {
        AccountGroupNameSearch = createSearchBuilder();
        AccountGroupNameSearch.and("accountId", AccountGroupNameSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountGroupNameSearch.and("name", AccountGroupNameSearch.entity().getName(), SearchCriteria.Op.EQ);
        AccountGroupNameSearch.done();

        AccountSearch = createSearchBuilder();
        AccountSearch.and("accountId", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.done();

        GroupSearch = createSearchBuilder();
        GroupSearch.and("groupId", GroupSearch.entity().getId(), SearchCriteria.Op.EQ);
        GroupSearch.done();
    }

    @Override
    public List<SecurityGroupRulesVO> listSecurityGroupRules(final long accountId, final String groupName) {
        final Filter searchFilter = new Filter(SecurityGroupRulesVO.class, "id", true, null, null);

        final SearchCriteria<SecurityGroupRulesVO> sc = AccountGroupNameSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("name", groupName);

        return listBy(sc, searchFilter);
    }

    @Override
    public List<SecurityGroupRulesVO> listSecurityGroupRules(final long accountId) {
        final Filter searchFilter = new Filter(SecurityGroupRulesVO.class, "id", true, null, null);
        final SearchCriteria<SecurityGroupRulesVO> sc = AccountSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc, searchFilter);
    }

    @Override
    public List<SecurityGroupRulesVO> listSecurityGroupRules() {
        final Filter searchFilter = new Filter(SecurityGroupRulesVO.class, "id", true, null, null);
        return listAll(searchFilter);
    }

    @Override
    public List<SecurityGroupRulesVO> listSecurityRulesByGroupId(final long groupId) {
        final Filter searchFilter = new Filter(SecurityGroupRulesVO.class, "id", true, null, null);
        final SearchCriteria<SecurityGroupRulesVO> sc = GroupSearch.create();
        sc.setParameters("groupId", groupId);
        return listBy(sc, searchFilter);
    }
}

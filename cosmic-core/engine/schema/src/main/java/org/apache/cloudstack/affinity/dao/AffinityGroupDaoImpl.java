package org.apache.cloudstack.affinity.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.affinity.AffinityGroupDomainMapVO;
import org.apache.cloudstack.affinity.AffinityGroupVO;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

public class AffinityGroupDaoImpl extends GenericDaoBase<AffinityGroupVO, Long> implements AffinityGroupDao {
    @Inject
    AffinityGroupDomainMapDao _groupDomainDao;
    private SearchBuilder<AffinityGroupVO> AccountIdSearch;
    private SearchBuilder<AffinityGroupVO> AccountIdNameSearch;
    private SearchBuilder<AffinityGroupVO> AccountIdNamesSearch;
    private SearchBuilder<AffinityGroupVO> DomainLevelNameSearch;
    private SearchBuilder<AffinityGroupVO> AccountIdTypeSearch;
    private SearchBuilder<AffinityGroupVO> DomainLevelTypeSearch;

    public AffinityGroupDaoImpl() {

    }

    @PostConstruct
    protected void init() {
        AccountIdSearch = createSearchBuilder();
        AccountIdSearch.and("accountId", AccountIdSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountIdSearch.done();

        AccountIdNameSearch = createSearchBuilder();
        AccountIdNameSearch.and("accountId", AccountIdNameSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountIdNameSearch.and("name", AccountIdNameSearch.entity().getName(), SearchCriteria.Op.EQ);

        AccountIdNamesSearch = createSearchBuilder();
        AccountIdNamesSearch.and("accountId", AccountIdNamesSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountIdNamesSearch.and("groupNames", AccountIdNamesSearch.entity().getName(), SearchCriteria.Op.IN);
        AccountIdNameSearch.done();

        final SearchBuilder<AffinityGroupDomainMapVO> domainMapSearch = _groupDomainDao.createSearchBuilder();
        domainMapSearch.and("domainId", domainMapSearch.entity().getDomainId(), SearchCriteria.Op.EQ);

        DomainLevelNameSearch = createSearchBuilder();
        DomainLevelNameSearch.and("name", DomainLevelNameSearch.entity().getName(), SearchCriteria.Op.EQ);
        DomainLevelNameSearch.and("aclType", DomainLevelNameSearch.entity().getAclType(), SearchCriteria.Op.EQ);
        DomainLevelNameSearch.join("domainMapSearch", domainMapSearch, domainMapSearch.entity().getAffinityGroupId(), DomainLevelNameSearch.entity().getId(),
                JoinType.INNER);
        DomainLevelNameSearch.done();

        AccountIdTypeSearch = createSearchBuilder();
        AccountIdTypeSearch.and("accountId", AccountIdTypeSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountIdTypeSearch.and("type", AccountIdTypeSearch.entity().getType(), SearchCriteria.Op.EQ);

        final SearchBuilder<AffinityGroupDomainMapVO> domainTypeSearch = _groupDomainDao.createSearchBuilder();
        domainTypeSearch.and("domainId", domainTypeSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        DomainLevelTypeSearch = createSearchBuilder();
        DomainLevelTypeSearch.and("type", DomainLevelTypeSearch.entity().getType(), SearchCriteria.Op.EQ);
        DomainLevelTypeSearch.and("aclType", DomainLevelTypeSearch.entity().getAclType(), SearchCriteria.Op.EQ);
        DomainLevelTypeSearch.join("domainTypeSearch", domainTypeSearch, domainTypeSearch.entity().getAffinityGroupId(), DomainLevelTypeSearch.entity().getId(),
                JoinType.INNER);
        DomainLevelTypeSearch.done();
    }

    @Override
    public List<AffinityGroupVO> listByAccountId(final long accountId) {
        final SearchCriteria<AffinityGroupVO> sc = AccountIdSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public boolean isNameInUse(final Long accountId, final Long domainId, final String name) {
        final SearchCriteria<AffinityGroupVO> sc = createSearchCriteria();
        sc.addAnd("name", SearchCriteria.Op.EQ, name);
        if (accountId != null) {
            sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        } else {
            sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
            sc.addAnd("accountId", SearchCriteria.Op.NULL);
        }

        final List<AffinityGroupVO> AffinityGroups = listBy(sc);
        return ((AffinityGroups != null) && !AffinityGroups.isEmpty());
    }

    @Override
    public AffinityGroupVO findByAccountAndName(final Long accountId, final String name) {
        final SearchCriteria<AffinityGroupVO> sc = AccountIdNameSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("name", name);

        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public List<AffinityGroupVO> findByAccountAndNames(final Long accountId, final String... names) {
        final SearchCriteria<AffinityGroupVO> sc = AccountIdNamesSearch.create();
        sc.setParameters("accountId", accountId);

        sc.setParameters("groupNames", (Object[]) names);

        return listBy(sc);
    }

    @Override
    public int removeByAccountId(final long accountId) {
        final SearchCriteria<AffinityGroupVO> sc = AccountIdSearch.create();
        sc.setParameters("accountId", accountId);
        return expunge(sc);
    }

    @Override
    public AffinityGroupVO findDomainLevelGroupByName(final Long domainId, final String affinityGroupName) {
        final SearchCriteria<AffinityGroupVO> sc = DomainLevelNameSearch.create();
        sc.setParameters("aclType", ControlledEntity.ACLType.Domain);
        sc.setParameters("name", affinityGroupName);
        sc.setJoinParameters("domainMapSearch", "domainId", domainId);
        return findOneBy(sc);
    }

    @Override
    public AffinityGroupVO findByAccountAndType(final Long accountId, final String type) {
        final SearchCriteria<AffinityGroupVO> sc = AccountIdTypeSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("type", type);

        return findOneBy(sc);
    }

    @Override
    public AffinityGroupVO findDomainLevelGroupByType(final Long domainId, final String type) {
        final SearchCriteria<AffinityGroupVO> sc = DomainLevelTypeSearch.create();
        sc.setParameters("aclType", ControlledEntity.ACLType.Domain);
        sc.setParameters("type", type);
        sc.setJoinParameters("domainTypeSearch", "domainId", domainId);
        return findOneBy(sc);
    }
}

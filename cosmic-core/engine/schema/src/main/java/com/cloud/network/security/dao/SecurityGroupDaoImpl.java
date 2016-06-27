package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupVO;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SecurityGroupDaoImpl extends GenericDaoBase<SecurityGroupVO, Long> implements SecurityGroupDao {
    @Inject
    ResourceTagDao _tagsDao;
    private final SearchBuilder<SecurityGroupVO> AccountIdSearch;
    private final SearchBuilder<SecurityGroupVO> AccountIdNameSearch;
    private final SearchBuilder<SecurityGroupVO> AccountIdNamesSearch;

    protected SecurityGroupDaoImpl() {
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
    }

    @Override
    public List<SecurityGroupVO> listByAccountId(final long accountId) {
        final SearchCriteria<SecurityGroupVO> sc = AccountIdSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public boolean isNameInUse(final Long accountId, final Long domainId, final String name) {
        final SearchCriteria<SecurityGroupVO> sc = createSearchCriteria();
        sc.addAnd("name", SearchCriteria.Op.EQ, name);
        if (accountId != null) {
            sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);
        } else {
            sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
            sc.addAnd("accountId", SearchCriteria.Op.NULL);
        }

        final List<SecurityGroupVO> securityGroups = listBy(sc);
        return ((securityGroups != null) && !securityGroups.isEmpty());
    }

    @Override
    public SecurityGroupVO findByAccountAndName(final Long accountId, final String name) {
        final SearchCriteria<SecurityGroupVO> sc = AccountIdNameSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("name", name);

        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public List<SecurityGroupVO> findByAccountAndNames(final Long accountId, final String... names) {
        final SearchCriteria<SecurityGroupVO> sc = AccountIdNamesSearch.create();
        sc.setParameters("accountId", accountId);

        sc.setParameters("groupNames", (Object[]) names);

        return listBy(sc);
    }

    @Override
    public int removeByAccountId(final long accountId) {
        final SearchCriteria<SecurityGroupVO> sc = AccountIdSearch.create();
        sc.setParameters("accountId", accountId);
        return expunge(sc);
    }

    @Override
    @DB
    public boolean expunge(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SecurityGroupVO entry = findById(id);
        if (entry != null) {
            _tagsDao.removeByIdAndType(id, ResourceObjectType.SecurityGroup);
        }
        final boolean result = super.expunge(id);
        txn.commit();
        return result;
    }

    @Override
    @DB
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SecurityGroupVO entry = findById(id);
        if (entry != null) {
            _tagsDao.removeByIdAndType(id, ResourceObjectType.SecurityGroup);
        }
        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}

package com.cloud.configuration.dao;

import com.cloud.configuration.Resource;
import com.cloud.configuration.Resource.ResourceOwnerType;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.configuration.ResourceCountVO;
import com.cloud.configuration.ResourceLimit;
import com.cloud.domain.dao.DomainDao;
import com.cloud.exception.UnsupportedServiceException;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ResourceCountDaoImpl extends GenericDaoBase<ResourceCountVO, Long> implements ResourceCountDao {
    private final SearchBuilder<ResourceCountVO> TypeSearch;

    private final SearchBuilder<ResourceCountVO> AccountSearch;
    private final SearchBuilder<ResourceCountVO> DomainSearch;

    @Inject
    protected DomainDao _domainDao;
    @Inject
    protected AccountDao _accountDao;

    public ResourceCountDaoImpl() {
        TypeSearch = createSearchBuilder();
        TypeSearch.and("type", TypeSearch.entity().getType(), SearchCriteria.Op.EQ);
        TypeSearch.and("accountId", TypeSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        TypeSearch.and("domainId", TypeSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        TypeSearch.done();

        AccountSearch = createSearchBuilder();
        AccountSearch.and("accountId", AccountSearch.entity().getAccountId(), SearchCriteria.Op.NNULL);
        AccountSearch.done();

        DomainSearch = createSearchBuilder();
        DomainSearch.and("domainId", DomainSearch.entity().getDomainId(), SearchCriteria.Op.NNULL);
        DomainSearch.done();
    }

    @Override
    public long getResourceCount(final long ownerId, final ResourceOwnerType ownerType, final ResourceType type) {
        final ResourceCountVO vo = findByOwnerAndType(ownerId, ownerType, type);
        if (vo != null) {
            return vo.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public void setResourceCount(final long ownerId, final ResourceOwnerType ownerType, final ResourceType type, final long count) {
        final ResourceCountVO resourceCountVO = findByOwnerAndType(ownerId, ownerType, type);
        if (resourceCountVO != null && count != resourceCountVO.getCount()) {
            resourceCountVO.setCount(count);
            update(resourceCountVO.getId(), resourceCountVO);
        }
    }

    @Override
    @Deprecated
    public void updateDomainCount(final long domainId, final ResourceType type, final boolean increment, long delta) {
        delta = increment ? delta : delta * -1;

        final ResourceCountVO resourceCountVO = findByOwnerAndType(domainId, ResourceOwnerType.Domain, type);
        resourceCountVO.setCount(resourceCountVO.getCount() + delta);
        update(resourceCountVO.getId(), resourceCountVO);
    }

    @Override
    public boolean updateById(final long id, final boolean increment, long delta) {
        delta = increment ? delta : delta * -1;

        final ResourceCountVO resourceCountVO = findById(id);
        resourceCountVO.setCount(resourceCountVO.getCount() + delta);
        return update(resourceCountVO.getId(), resourceCountVO);
    }

    @Override
    @DB
    public void createResourceCounts(final long ownerId, final ResourceLimit.ResourceOwnerType ownerType) {

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final ResourceType[] resourceTypes = Resource.ResourceType.values();
        for (final ResourceType resourceType : resourceTypes) {
            if (!resourceType.supportsOwner(ownerType)) {
                continue;
            }
            final ResourceCountVO resourceCountVO = new ResourceCountVO(resourceType, 0, ownerId, ownerType);
            persist(resourceCountVO);
        }

        txn.commit();
    }

    @Override
    public List<ResourceCountVO> listByOwnerId(final long ownerId, final ResourceOwnerType ownerType) {
        if (ownerType == ResourceOwnerType.Account) {
            return listByAccountId(ownerId);
        } else if (ownerType == ResourceOwnerType.Domain) {
            return listByDomainId(ownerId);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public ResourceCountVO findByOwnerAndType(final long ownerId, final ResourceOwnerType ownerType, final ResourceType type) {
        final SearchCriteria<ResourceCountVO> sc = TypeSearch.create();
        sc.setParameters("type", type);

        if (ownerType == ResourceOwnerType.Account) {
            sc.setParameters("accountId", ownerId);
            return findOneIncludingRemovedBy(sc);
        } else if (ownerType == ResourceOwnerType.Domain) {
            sc.setParameters("domainId", ownerId);
            return findOneIncludingRemovedBy(sc);
        } else {
            return null;
        }
    }

    @Override
    public List<ResourceCountVO> listResourceCountByOwnerType(final ResourceOwnerType ownerType) {
        if (ownerType == ResourceOwnerType.Account) {
            return listBy(AccountSearch.create());
        } else if (ownerType == ResourceOwnerType.Domain) {
            return listBy(DomainSearch.create());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Set<Long> listAllRowsToUpdate(final long ownerId, final ResourceOwnerType ownerType, final ResourceType type) {
        final Set<Long> rowIds = new HashSet<>();

        if (ownerType == ResourceOwnerType.Account) {
            //get records for account
            final ResourceCountVO accountCountRecord = findByOwnerAndType(ownerId, ResourceOwnerType.Account, type);
            if (accountCountRecord != null) {
                rowIds.add(accountCountRecord.getId());
            }

            //get records for account's domain and all its parent domains
            rowIds.addAll(listRowsToUpdateForDomain(_accountDao.findByIdIncludingRemoved(ownerId).getDomainId(), type));
        } else if (ownerType == ResourceOwnerType.Domain) {
            return listRowsToUpdateForDomain(ownerId, type);
        }

        return rowIds;
    }

    @Override
    public Set<Long> listRowsToUpdateForDomain(final long domainId, final ResourceType type) {
        final Set<Long> rowIds = new HashSet<>();
        final Set<Long> domainIdsToUpdate = _domainDao.getDomainParentIds(domainId);
        for (final Long domainIdToUpdate : domainIdsToUpdate) {
            final ResourceCountVO domainCountRecord = findByOwnerAndType(domainIdToUpdate, ResourceOwnerType.Domain, type);
            if (domainCountRecord != null) {
                rowIds.add(domainCountRecord.getId());
            }
        }
        return rowIds;
    }

    @Override
    public long removeEntriesByOwner(final long ownerId, final ResourceOwnerType ownerType) {
        final SearchCriteria<ResourceCountVO> sc = TypeSearch.create();

        if (ownerType == ResourceOwnerType.Account) {
            sc.setParameters("accountId", ownerId);
            return remove(sc);
        } else if (ownerType == ResourceOwnerType.Domain) {
            sc.setParameters("domainId", ownerId);
            return remove(sc);
        }
        return 0;
    }

    private List<ResourceCountVO> listByAccountId(final long accountId) {
        final SearchCriteria<ResourceCountVO> sc = TypeSearch.create();
        sc.setParameters("accountId", accountId);

        return listBy(sc);
    }

    private List<ResourceCountVO> listByDomainId(final long domainId) {
        final SearchCriteria<ResourceCountVO> sc = TypeSearch.create();
        sc.setParameters("domainId", domainId);

        return listBy(sc);
    }

    @Override
    public ResourceCountVO persist(final ResourceCountVO resourceCountVO) {
        final ResourceOwnerType ownerType = resourceCountVO.getResourceOwnerType();
        final ResourceType resourceType = resourceCountVO.getType();
        if (!resourceType.supportsOwner(ownerType)) {
            throw new UnsupportedServiceException("Resource type " + resourceType + " is not supported for owner of type " + ownerType.getName());
        }

        return super.persist(resourceCountVO);
    }
}

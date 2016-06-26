package com.cloud.configuration.dao;

import com.cloud.configuration.Resource;
import com.cloud.configuration.Resource.ResourceOwnerType;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.configuration.ResourceCount;
import com.cloud.configuration.ResourceLimitVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ResourceLimitDaoImpl extends GenericDaoBase<ResourceLimitVO, Long> implements ResourceLimitDao {
    private final SearchBuilder<ResourceLimitVO> IdTypeSearch;

    public ResourceLimitDaoImpl() {
        IdTypeSearch = createSearchBuilder();
        IdTypeSearch.and("type", IdTypeSearch.entity().getType(), SearchCriteria.Op.EQ);
        IdTypeSearch.and("domainId", IdTypeSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        IdTypeSearch.and("accountId", IdTypeSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        IdTypeSearch.done();
    }

    @Override
    public List<ResourceLimitVO> listByOwner(final Long ownerId, final ResourceOwnerType ownerType) {
        final SearchCriteria<ResourceLimitVO> sc = IdTypeSearch.create();

        if (ownerType == ResourceOwnerType.Account) {
            sc.setParameters("accountId", ownerId);
            return listBy(sc);
        } else if (ownerType == ResourceOwnerType.Domain) {
            sc.setParameters("domainId", ownerId);
            return listBy(sc);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean update(final Long id, final Long max) {
        final ResourceLimitVO limit = findById(id);
        if (max != null) {
            limit.setMax(max);
        } else {
            limit.setMax(new Long(-1));
        }
        return update(id, limit);
    }

    @Override
    public ResourceCount.ResourceType getLimitType(final String type) {
        final ResourceType[] validTypes = Resource.ResourceType.values();

        for (final ResourceType validType : validTypes) {
            if (validType.getName().equals(type)) {
                return validType;
            }
        }
        return null;
    }

    @Override
    public ResourceLimitVO findByOwnerIdAndType(final long ownerId, final ResourceOwnerType ownerType, final ResourceCount.ResourceType type) {
        final SearchCriteria<ResourceLimitVO> sc = IdTypeSearch.create();
        sc.setParameters("type", type);

        if (ownerType == ResourceOwnerType.Account) {
            sc.setParameters("accountId", ownerId);
            return findOneBy(sc);
        } else if (ownerType == ResourceOwnerType.Domain) {
            sc.setParameters("domainId", ownerId);
            return findOneBy(sc);
        } else {
            return null;
        }
    }

    @Override
    public long removeEntriesByOwner(final Long ownerId, final ResourceOwnerType ownerType) {
        final SearchCriteria<ResourceLimitVO> sc = IdTypeSearch.create();

        if (ownerType == ResourceOwnerType.Account) {
            sc.setParameters("accountId", ownerId);
            return remove(sc);
        } else if (ownerType == ResourceOwnerType.Domain) {
            sc.setParameters("domainId", ownerId);
            return remove(sc);
        }
        return 0;
    }
}

package com.cloud.configuration.dao;

import com.cloud.configuration.Resource.ResourceOwnerType;
import com.cloud.configuration.ResourceCount;
import com.cloud.configuration.ResourceLimitVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ResourceLimitDao extends GenericDao<ResourceLimitVO, Long> {

    List<ResourceLimitVO> listByOwner(Long ownerId, ResourceOwnerType ownerType);

    boolean update(Long id, Long max);

    ResourceCount.ResourceType getLimitType(String type);

    ResourceLimitVO findByOwnerIdAndType(long ownerId, ResourceOwnerType ownerType, ResourceCount.ResourceType type);

    long removeEntriesByOwner(Long ownerId, ResourceOwnerType ownerType);
}

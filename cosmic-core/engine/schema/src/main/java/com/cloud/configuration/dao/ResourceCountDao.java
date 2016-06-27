package com.cloud.configuration.dao;

import com.cloud.configuration.Resource.ResourceOwnerType;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.configuration.ResourceCountVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Set;

public interface ResourceCountDao extends GenericDao<ResourceCountVO, Long> {
    /**
     * @param domainId the id of the domain to get the resource count
     * @param type     the type of resource (e.g. user_vm, public_ip, volume)
     * @return the count of resources in use for the given type and domain
     */
    long getResourceCount(long ownerId, ResourceOwnerType ownerType, ResourceType type);

    /**
     * @param domainId the id of the domain to set the resource count
     * @param type     the type of resource (e.g. user_vm, public_ip, volume)
     * @param the      count of resources in use for the given type and domain
     */
    void setResourceCount(long ownerId, ResourceOwnerType ownerType, ResourceType type, long count);

    @Deprecated
    void updateDomainCount(long domainId, ResourceType type, boolean increment, long delta);

    boolean updateById(long id, boolean increment, long delta);

    void createResourceCounts(long ownerId, ResourceOwnerType ownerType);

    List<ResourceCountVO> listByOwnerId(long ownerId, ResourceOwnerType ownerType);

    ResourceCountVO findByOwnerAndType(long ownerId, ResourceOwnerType ownerType, ResourceType type);

    List<ResourceCountVO> listResourceCountByOwnerType(ResourceOwnerType ownerType);

    Set<Long> listAllRowsToUpdate(long ownerId, ResourceOwnerType ownerType, ResourceType type);

    Set<Long> listRowsToUpdateForDomain(long domainId, ResourceType type);

    long removeEntriesByOwner(long ownerId, ResourceOwnerType ownerType);
}

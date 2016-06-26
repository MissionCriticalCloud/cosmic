package com.cloud.server;

import com.cloud.server.ResourceTag.ResourceObjectType;

import java.util.List;
import java.util.Map;

public interface TaggedResourceService {

    /**
     * @param resourceIds  TODO
     * @param resourceType
     * @param tags
     * @param customer     TODO
     * @return
     */
    List<ResourceTag> createTags(List<String> resourceIds, ResourceObjectType resourceType, Map<String, String> tags, String customer);

    /**
     * @param resourceIds
     * @param resourceType
     * @param tags
     * @return
     */
    boolean deleteTags(List<String> resourceIds, ResourceObjectType resourceType, Map<String, String> tags);

    List<? extends ResourceTag> listByResourceTypeAndId(ResourceObjectType type, long resourceId);

    //FIXME - the methods below should be extracted to its separate manager/service responsible just for retrieving object details
    ResourceObjectType getResourceType(String resourceTypeStr);

    /**
     * @param resourceId
     * @param resourceType
     * @return
     */
    String getUuid(String resourceId, ResourceObjectType resourceType);

    public long getResourceId(String resourceId, ResourceObjectType resourceType);
}

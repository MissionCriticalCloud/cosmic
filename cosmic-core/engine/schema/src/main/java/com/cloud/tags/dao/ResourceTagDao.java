package com.cloud.tags.dao;

import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.ResourceTagVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ResourceTagDao extends GenericDao<ResourceTagVO, Long> {

    /**
     * @param resourceId
     * @param resourceType
     * @return
     */
    boolean removeByIdAndType(long resourceId, ResourceObjectType resourceType);

    List<? extends ResourceTag> listBy(long resourceId, ResourceObjectType resourceType);

    void updateResourceId(long srcId, long destId, ResourceObjectType resourceType);
}

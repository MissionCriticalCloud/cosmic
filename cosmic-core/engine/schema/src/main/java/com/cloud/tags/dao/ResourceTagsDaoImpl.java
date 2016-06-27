package com.cloud.tags.dao;

import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.ResourceTagVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ResourceTagsDaoImpl extends GenericDaoBase<ResourceTagVO, Long> implements ResourceTagDao {
    final SearchBuilder<ResourceTagVO> AllFieldsSearch;

    public ResourceTagsDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("resourceId", AllFieldsSearch.entity().getResourceId(), Op.EQ);
        AllFieldsSearch.and("uuid", AllFieldsSearch.entity().getResourceUuid(), Op.EQ);
        AllFieldsSearch.and("resourceType", AllFieldsSearch.entity().getResourceType(), Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public boolean removeByIdAndType(final long resourceId, final ResourceTag.ResourceObjectType resourceType) {
        final SearchCriteria<ResourceTagVO> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", resourceId);
        sc.setParameters("resourceType", resourceType);
        remove(sc);
        return true;
    }

    @Override
    public List<? extends ResourceTag> listBy(final long resourceId, final ResourceObjectType resourceType) {
        final SearchCriteria<ResourceTagVO> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", resourceId);
        sc.setParameters("resourceType", resourceType);
        return listBy(sc);
    }

    @Override
    public void updateResourceId(final long srcId, final long destId, final ResourceObjectType resourceType) {
        final SearchCriteria<ResourceTagVO> sc = AllFieldsSearch.create();
        sc.setParameters("resourceId", srcId);
        sc.setParameters("resourceType", resourceType);
        for (final ResourceTagVO tag : listBy(sc)) {
            tag.setResourceId(destId);
            update(tag.getId(), tag);
        }
    }
}

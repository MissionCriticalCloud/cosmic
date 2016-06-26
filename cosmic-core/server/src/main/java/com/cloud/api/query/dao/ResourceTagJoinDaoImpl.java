package com.cloud.api.query.dao;

import com.cloud.api.ApiResponseHelper;
import com.cloud.api.query.vo.ResourceTagJoinVO;
import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import org.apache.cloudstack.api.response.ResourceTagResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceTagJoinDaoImpl extends GenericDaoBase<ResourceTagJoinVO, Long> implements ResourceTagJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(ResourceTagJoinDaoImpl.class);
    private final SearchBuilder<ResourceTagJoinVO> tagSearch;
    private final SearchBuilder<ResourceTagJoinVO> tagIdSearch;
    private final SearchBuilder<ResourceTagJoinVO> AllFieldsSearch;
    @Inject
    private ConfigurationDao _configDao;

    protected ResourceTagJoinDaoImpl() {

        tagSearch = createSearchBuilder();
        tagSearch.and("idIN", tagSearch.entity().getId(), SearchCriteria.Op.IN);
        tagSearch.done();

        tagIdSearch = createSearchBuilder();
        tagIdSearch.and("id", tagIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        tagIdSearch.done();

        this._count = "select count(distinct id) from resource_tag_view WHERE ";

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("resourceId", AllFieldsSearch.entity().getResourceId(), Op.EQ);
        AllFieldsSearch.and("uuid", AllFieldsSearch.entity().getResourceUuid(), Op.EQ);
        AllFieldsSearch.and("resourceType", AllFieldsSearch.entity().getResourceType(), Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public ResourceTagResponse newResourceTagResponse(final ResourceTagJoinVO resourceTag, final boolean keyValueOnly) {
        final ResourceTagResponse response = new ResourceTagResponse();
        response.setKey(resourceTag.getKey());
        response.setValue(resourceTag.getValue());

        if (!keyValueOnly) {
            response.setResourceType(resourceTag.getResourceType().toString());
            response.setResourceId(resourceTag.getResourceUuid());

            ApiResponseHelper.populateOwner(response, resourceTag);

            response.setDomainId(resourceTag.getDomainUuid());
            response.setDomainName(resourceTag.getDomainName());

            response.setCustomer(resourceTag.getCustomer());
        }

        response.setObjectName("tag");

        return response;
    }

    @Override
    public ResourceTagJoinVO newResourceTagView(final ResourceTag vr) {
        final SearchCriteria<ResourceTagJoinVO> sc = tagIdSearch.create();
        sc.setParameters("id", vr.getId());
        final List<ResourceTagJoinVO> tags = searchIncludingRemoved(sc, null, null, false);
        if (tags != null && tags.size() > 0) {
            return tags.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<ResourceTagJoinVO> searchByIds(final Long... tagIds) {
        // set detail batch query size
        int DETAILS_BATCH_SIZE = 2000;
        final String batchCfg = _configDao.getValue("detail.batch.query.size");
        if (batchCfg != null) {
            DETAILS_BATCH_SIZE = Integer.parseInt(batchCfg);
        }
        // query details by batches
        final List<ResourceTagJoinVO> uvList = new ArrayList<>();
        // query details by batches
        int curr_index = 0;
        if (tagIds.length > DETAILS_BATCH_SIZE) {
            while ((curr_index + DETAILS_BATCH_SIZE) <= tagIds.length) {
                final Long[] ids = new Long[DETAILS_BATCH_SIZE];
                for (int k = 0, j = curr_index; j < curr_index + DETAILS_BATCH_SIZE; j++, k++) {
                    ids[k] = tagIds[j];
                }
                final SearchCriteria<ResourceTagJoinVO> sc = tagSearch.create();
                sc.setParameters("idIN", ids);
                final List<ResourceTagJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
                if (vms != null) {
                    uvList.addAll(vms);
                }
                curr_index += DETAILS_BATCH_SIZE;
            }
        }
        if (curr_index < tagIds.length) {
            final int batch_size = (tagIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];
            for (int k = 0, j = curr_index; j < curr_index + batch_size; j++, k++) {
                ids[k] = tagIds[j];
            }
            final SearchCriteria<ResourceTagJoinVO> sc = tagSearch.create();
            sc.setParameters("idIN", ids);
            final List<ResourceTagJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
            if (vms != null) {
                uvList.addAll(vms);
            }
        }
        return uvList;
    }

    @Override
    public List<ResourceTagJoinVO> listBy(final String resourceUUID, final ResourceObjectType resourceType) {
        final SearchCriteria<ResourceTagJoinVO> sc = AllFieldsSearch.create();
        sc.setParameters("uuid", resourceUUID);
        sc.setParameters("resourceType", resourceType);
        return listBy(sc);
    }

    @Override
    public ResourceTagJoinVO searchById(final Long id) {
        final SearchCriteria<ResourceTagJoinVO> sc = tagIdSearch.create();
        sc.setParameters("id", id);
        final List<ResourceTagJoinVO> tags = searchIncludingRemoved(sc, null, null, false);
        if (tags != null && tags.size() > 0) {
            return tags.get(0);
        } else {
            return null;
        }
    }
}

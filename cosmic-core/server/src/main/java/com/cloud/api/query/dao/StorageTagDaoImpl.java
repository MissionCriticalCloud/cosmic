package com.cloud.api.query.dao;

import com.cloud.api.query.vo.StorageTagVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.response.StorageTagResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StorageTagDaoImpl extends GenericDaoBase<StorageTagVO, Long> implements StorageTagDao {
    public static final Logger s_logger = LoggerFactory.getLogger(StorageTagDaoImpl.class);
    private final SearchBuilder<StorageTagVO> stSearch;
    private final SearchBuilder<StorageTagVO> stIdSearch;
    @Inject
    private ConfigurationDao _configDao;

    protected StorageTagDaoImpl() {
        stSearch = createSearchBuilder();

        stSearch.and("idIN", stSearch.entity().getId(), SearchCriteria.Op.IN);
        stSearch.done();

        stIdSearch = createSearchBuilder();

        stIdSearch.and("id", stIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        stIdSearch.done();

        _count = "select count(distinct id) from storage_tag_view WHERE ";
    }

    @Override
    public StorageTagResponse newStorageTagResponse(final StorageTagVO tag) {
        final StorageTagResponse tagResponse = new StorageTagResponse();

        tagResponse.setName(tag.getName());
        tagResponse.setPoolId(tag.getPoolId());

        tagResponse.setObjectName("storagetag");

        return tagResponse;
    }

    @Override
    public List<StorageTagVO> searchByIds(final Long... stIds) {
        final String batchCfg = _configDao.getValue("detail.batch.query.size");

        final int detailsBatchSize = batchCfg != null ? Integer.parseInt(batchCfg) : 2000;

        // query details by batches
        final List<StorageTagVO> uvList = new ArrayList<>();
        int curr_index = 0;

        if (stIds.length > detailsBatchSize) {
            while ((curr_index + detailsBatchSize) <= stIds.length) {
                final Long[] ids = new Long[detailsBatchSize];

                for (int k = 0, j = curr_index; j < curr_index + detailsBatchSize; j++, k++) {
                    ids[k] = stIds[j];
                }

                final SearchCriteria<StorageTagVO> sc = stSearch.create();

                sc.setParameters("idIN", (Object[]) ids);

                final List<StorageTagVO> vms = searchIncludingRemoved(sc, null, null, false);

                if (vms != null) {
                    uvList.addAll(vms);
                }

                curr_index += detailsBatchSize;
            }
        }

        if (curr_index < stIds.length) {
            final int batch_size = (stIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];

            for (int k = 0, j = curr_index; j < curr_index + batch_size; j++, k++) {
                ids[k] = stIds[j];
            }

            final SearchCriteria<StorageTagVO> sc = stSearch.create();

            sc.setParameters("idIN", (Object[]) ids);

            final List<StorageTagVO> vms = searchIncludingRemoved(sc, null, null, false);

            if (vms != null) {
                uvList.addAll(vms);
            }
        }

        return uvList;
    }
}

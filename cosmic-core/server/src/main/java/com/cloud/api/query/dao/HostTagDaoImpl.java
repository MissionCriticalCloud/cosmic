package com.cloud.api.query.dao;

import com.cloud.api.query.vo.HostTagVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.response.HostTagResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HostTagDaoImpl extends GenericDaoBase<HostTagVO, Long> implements HostTagDao {
    public static final Logger s_logger = LoggerFactory.getLogger(HostTagDaoImpl.class);
    private final SearchBuilder<HostTagVO> stSearch;
    private final SearchBuilder<HostTagVO> stIdSearch;
    @Inject
    private ConfigurationDao _configDao;

    protected HostTagDaoImpl() {
        stSearch = createSearchBuilder();

        stSearch.and("idIN", stSearch.entity().getId(), SearchCriteria.Op.IN);
        stSearch.done();

        stIdSearch = createSearchBuilder();

        stIdSearch.and("id", stIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        stIdSearch.done();

        _count = "select count(distinct id) from host_tags WHERE ";
    }

    @Override
    public HostTagResponse newHostTagResponse(final HostTagVO tag) {
        final HostTagResponse tagResponse = new HostTagResponse();

        tagResponse.setName(tag.getName());
        tagResponse.setHostId(tag.getHostId());

        tagResponse.setObjectName("hosttag");

        return tagResponse;
    }

    @Override
    public List<HostTagVO> searchByIds(final Long... stIds) {
        final String batchCfg = _configDao.getValue("detail.batch.query.size");

        final int detailsBatchSize = batchCfg != null ? Integer.parseInt(batchCfg) : 2000;

        // query details by batches
        final List<HostTagVO> uvList = new ArrayList<>();
        int curr_index = 0;

        if (stIds.length > detailsBatchSize) {
            while ((curr_index + detailsBatchSize) <= stIds.length) {
                final Long[] ids = new Long[detailsBatchSize];

                for (int k = 0, j = curr_index; j < curr_index + detailsBatchSize; j++, k++) {
                    ids[k] = stIds[j];
                }

                final SearchCriteria<HostTagVO> sc = stSearch.create();

                sc.setParameters("idIN", (Object[]) ids);

                final List<HostTagVO> vms = searchIncludingRemoved(sc, null, null, false);

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

            final SearchCriteria<HostTagVO> sc = stSearch.create();

            sc.setParameters("idIN", (Object[]) ids);

            final List<HostTagVO> vms = searchIncludingRemoved(sc, null, null, false);

            if (vms != null) {
                uvList.addAll(vms);
            }
        }

        return uvList;
    }
}

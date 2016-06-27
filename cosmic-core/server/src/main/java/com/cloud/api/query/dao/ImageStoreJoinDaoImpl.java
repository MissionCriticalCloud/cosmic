package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ImageStoreJoinVO;
import com.cloud.storage.ImageStore;
import com.cloud.utils.StringUtils;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.response.ImageStoreDetailResponse;
import org.apache.cloudstack.api.response.ImageStoreResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageStoreJoinDaoImpl extends GenericDaoBase<ImageStoreJoinVO, Long> implements ImageStoreJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(ImageStoreJoinDaoImpl.class);
    private final SearchBuilder<ImageStoreJoinVO> dsSearch;
    private final SearchBuilder<ImageStoreJoinVO> dsIdSearch;
    @Inject
    private ConfigurationDao _configDao;

    protected ImageStoreJoinDaoImpl() {

        dsSearch = createSearchBuilder();
        dsSearch.and("idIN", dsSearch.entity().getId(), SearchCriteria.Op.IN);
        dsSearch.done();

        dsIdSearch = createSearchBuilder();
        dsIdSearch.and("id", dsIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        dsIdSearch.done();

        _count = "select count(distinct id) from image_store_view WHERE ";
    }

    @Override
    public ImageStoreResponse newImageStoreResponse(final ImageStoreJoinVO ids) {
        final ImageStoreResponse osResponse = new ImageStoreResponse();
        osResponse.setId(ids.getUuid());
        osResponse.setName(ids.getName());
        osResponse.setProviderName(ids.getProviderName());
        osResponse.setProtocol(ids.getProtocol());
        String url = ids.getUrl();
        //if store is type cifs, remove the password
        if (ids.getProtocol().equals("cifs".toString())) {
            url = StringUtils.cleanString(url);
        }
        osResponse.setUrl(url);
        osResponse.setScope(ids.getScope());
        osResponse.setZoneId(ids.getZoneUuid());
        osResponse.setZoneName(ids.getZoneName());

        final String detailName = ids.getDetailName();
        if (detailName != null && detailName.length() > 0 && !detailName.equals(ApiConstants.PASSWORD)) {
            String detailValue = ids.getDetailValue();
            if (detailName.equals(ApiConstants.KEY) || detailName.equals(ApiConstants.S3_SECRET_KEY)) {
                // ALWAYS return an empty value for the S3 secret key since that key is managed by Amazon and not CloudStack
                detailValue = "";
            }
            final ImageStoreDetailResponse osdResponse = new ImageStoreDetailResponse(detailName, detailValue);
            osResponse.addDetail(osdResponse);
        }
        osResponse.setObjectName("imagestore");
        return osResponse;
    }

    @Override
    public ImageStoreResponse setImageStoreResponse(final ImageStoreResponse response, final ImageStoreJoinVO ids) {
        final String detailName = ids.getDetailName();
        if (detailName != null && detailName.length() > 0 && !detailName.equals(ApiConstants.PASSWORD)) {
            String detailValue = ids.getDetailValue();
            if (detailName.equals(ApiConstants.KEY) || detailName.equals(ApiConstants.S3_SECRET_KEY)) {
                // ALWAYS return an empty value for the S3 secret key since that key is managed by Amazon and not CloudStack
                detailValue = "";
            }
            final ImageStoreDetailResponse osdResponse = new ImageStoreDetailResponse(detailName, detailValue);
            response.addDetail(osdResponse);
        }
        return response;
    }

    @Override
    public List<ImageStoreJoinVO> newImageStoreView(final ImageStore os) {
        final SearchCriteria<ImageStoreJoinVO> sc = dsIdSearch.create();
        sc.setParameters("id", os.getId());
        return searchIncludingRemoved(sc, null, null, false);
    }

    @Override
    public List<ImageStoreJoinVO> searchByIds(final Long... spIds) {
        // set detail batch query size
        int DETAILS_BATCH_SIZE = 2000;
        final String batchCfg = _configDao.getValue("detail.batch.query.size");
        if (batchCfg != null) {
            DETAILS_BATCH_SIZE = Integer.parseInt(batchCfg);
        }
        // query details by batches
        final List<ImageStoreJoinVO> uvList = new ArrayList<>();
        // query details by batches
        int curr_index = 0;
        if (spIds.length > DETAILS_BATCH_SIZE) {
            while ((curr_index + DETAILS_BATCH_SIZE) <= spIds.length) {
                final Long[] ids = new Long[DETAILS_BATCH_SIZE];
                for (int k = 0, j = curr_index; j < curr_index + DETAILS_BATCH_SIZE; j++, k++) {
                    ids[k] = spIds[j];
                }
                final SearchCriteria<ImageStoreJoinVO> sc = dsSearch.create();
                sc.setParameters("idIN", ids);
                final List<ImageStoreJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
                if (vms != null) {
                    uvList.addAll(vms);
                }
                curr_index += DETAILS_BATCH_SIZE;
            }
        }
        if (curr_index < spIds.length) {
            final int batch_size = (spIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];
            for (int k = 0, j = curr_index; j < curr_index + batch_size; j++, k++) {
                ids[k] = spIds[j];
            }
            final SearchCriteria<ImageStoreJoinVO> sc = dsSearch.create();
            sc.setParameters("idIN", ids);
            final List<ImageStoreJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
            if (vms != null) {
                uvList.addAll(vms);
            }
        }
        return uvList;
    }
}

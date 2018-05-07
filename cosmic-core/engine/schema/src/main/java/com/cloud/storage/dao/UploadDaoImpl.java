package com.cloud.storage.dao;

import com.cloud.legacymodel.storage.Upload.Mode;
import com.cloud.legacymodel.storage.UploadStatus;
import com.cloud.storage.UploadVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UploadDaoImpl extends GenericDaoBase<UploadVO, Long> implements UploadDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UploadDaoImpl.class.getName());
    protected static final String UPDATE_UPLOAD_INFO = "UPDATE upload SET upload_state = ?, upload_pct= ?, last_updated = ? "
            + ", upload_error_str = ?, upload_job_id = ? " + "WHERE host_id = ? and type_id = ? and type = ?";
    protected static final String UPLOADS_STATE_DC = "SELECT * FROM upload t, host h where t.host_id = h.id and h.data_center_id=? "
            + " and t.type_id=? and t.upload_state = ?";
    protected final SearchBuilder<UploadVO> typeUploadStatusSearch;
    protected final SearchBuilder<UploadVO> typeHostAndUploadStatusSearch;
    protected final SearchBuilder<UploadVO> typeModeAndStatusSearch;

    public UploadDaoImpl() {
        this.typeUploadStatusSearch = createSearchBuilder();
        this.typeUploadStatusSearch.and("type_id", this.typeUploadStatusSearch.entity().getTypeId(), SearchCriteria.Op.EQ);
        this.typeUploadStatusSearch.and("upload_state", this.typeUploadStatusSearch.entity().getUploadState(), SearchCriteria.Op.EQ);
        this.typeUploadStatusSearch.and("type", this.typeUploadStatusSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.typeUploadStatusSearch.done();

        this.typeHostAndUploadStatusSearch = createSearchBuilder();
        this.typeHostAndUploadStatusSearch.and("host_id", this.typeHostAndUploadStatusSearch.entity().getDataStoreId(), SearchCriteria.Op.EQ);
        this.typeHostAndUploadStatusSearch.and("upload_state", this.typeHostAndUploadStatusSearch.entity().getUploadState(), SearchCriteria.Op.EQ);
        this.typeHostAndUploadStatusSearch.done();

        this.typeModeAndStatusSearch = createSearchBuilder();
        this.typeModeAndStatusSearch.and("mode", this.typeModeAndStatusSearch.entity().getMode(), SearchCriteria.Op.EQ);
        this.typeModeAndStatusSearch.and("upload_state", this.typeModeAndStatusSearch.entity().getUploadState(), SearchCriteria.Op.EQ);
        this.typeModeAndStatusSearch.done();
    }

    @Override
    public List<UploadVO> listByTypeUploadStatus(final long typeId, final UploadVO.Type type, final UploadStatus uploadState) {
        final SearchCriteria<UploadVO> sc = this.typeUploadStatusSearch.create();
        sc.setParameters("type_id", typeId);
        sc.setParameters("type", type);
        sc.setParameters("upload_state", uploadState.toString());
        return listBy(sc);
    }

    @Override
    public List<UploadVO> listByHostAndUploadStatus(final long sserverId, final UploadStatus uploadState) {
        final SearchCriteria<UploadVO> sc = this.typeHostAndUploadStatusSearch.create();
        sc.setParameters("host_id", sserverId);
        sc.setParameters("upload_state", uploadState.toString());
        return listBy(sc);
    }

    @Override
    public List<UploadVO> listByModeAndStatus(final Mode mode, final UploadStatus uploadState) {
        final SearchCriteria<UploadVO> sc = this.typeModeAndStatusSearch.create();
        sc.setParameters("mode", mode.toString());
        sc.setParameters("upload_state", uploadState.toString());
        return listBy(sc);
    }
}

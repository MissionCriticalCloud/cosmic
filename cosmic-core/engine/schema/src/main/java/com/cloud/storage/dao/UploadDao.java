package com.cloud.storage.dao;

import com.cloud.legacymodel.storage.Upload.Mode;
import com.cloud.legacymodel.storage.Upload.Type;
import com.cloud.legacymodel.storage.UploadStatus;
import com.cloud.storage.UploadVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface UploadDao extends GenericDao<UploadVO, Long> {

    List<UploadVO> listByTypeUploadStatus(long typeId, Type type, UploadStatus uploadState);

    List<UploadVO> listByHostAndUploadStatus(long sserverId, UploadStatus uploadInProgress);

    List<UploadVO> listByModeAndStatus(Mode mode, UploadStatus uploadState);
}

package com.cloud.storage.dao;

import com.cloud.storage.Upload.Mode;
import com.cloud.storage.Upload.Status;
import com.cloud.storage.Upload.Type;
import com.cloud.storage.UploadVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface UploadDao extends GenericDao<UploadVO, Long> {

    List<UploadVO> listByTypeUploadStatus(long typeId, Type type, Status uploadState);

    List<UploadVO> listByHostAndUploadStatus(long sserverId, Status uploadInProgress);

    List<UploadVO> listByModeAndStatus(Mode mode, Status uploadState);
}

package com.cloud.storage.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface SnapshotDetailsDao extends GenericDao<SnapshotDetailsVO, Long>, ResourceDetailsDao<SnapshotDetailsVO> {
}

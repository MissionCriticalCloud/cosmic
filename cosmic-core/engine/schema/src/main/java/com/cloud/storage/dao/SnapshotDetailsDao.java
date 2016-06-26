package com.cloud.storage.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface SnapshotDetailsDao extends GenericDao<SnapshotDetailsVO, Long>, ResourceDetailsDao<SnapshotDetailsVO> {
}

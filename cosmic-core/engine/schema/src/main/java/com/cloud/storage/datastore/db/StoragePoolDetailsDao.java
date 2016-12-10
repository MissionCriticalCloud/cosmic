package com.cloud.storage.datastore.db;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface StoragePoolDetailsDao extends GenericDao<StoragePoolDetailVO, Long>, ResourceDetailsDao<StoragePoolDetailVO> {
}

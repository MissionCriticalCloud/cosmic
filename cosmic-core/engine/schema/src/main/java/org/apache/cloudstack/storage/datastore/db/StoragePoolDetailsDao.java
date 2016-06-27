package org.apache.cloudstack.storage.datastore.db;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface StoragePoolDetailsDao extends GenericDao<StoragePoolDetailVO, Long>, ResourceDetailsDao<StoragePoolDetailVO> {
}

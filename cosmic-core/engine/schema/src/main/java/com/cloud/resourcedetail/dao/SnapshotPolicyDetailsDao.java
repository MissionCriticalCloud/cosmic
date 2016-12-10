package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.resourcedetail.SnapshotPolicyDetailVO;
import com.cloud.utils.db.GenericDao;

public interface SnapshotPolicyDetailsDao extends GenericDao<SnapshotPolicyDetailVO, Long>, ResourceDetailsDao<SnapshotPolicyDetailVO> {
}

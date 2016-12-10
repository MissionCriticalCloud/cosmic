package com.cloud.vm.snapshot.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.snapshot.VMSnapshotDetailsVO;

public interface VMSnapshotDetailsDao extends GenericDao<VMSnapshotDetailsVO, Long>, ResourceDetailsDao<VMSnapshotDetailsVO> {
}

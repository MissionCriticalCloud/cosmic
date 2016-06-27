package com.cloud.vm.snapshot.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.snapshot.VMSnapshotDetailsVO;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface VMSnapshotDetailsDao extends GenericDao<VMSnapshotDetailsVO, Long>, ResourceDetailsDao<VMSnapshotDetailsVO> {
}

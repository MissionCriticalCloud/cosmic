package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.DiskOfferingDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface DiskOfferingDetailsDao extends GenericDao<DiskOfferingDetailVO, Long>, ResourceDetailsDao<DiskOfferingDetailVO> {

}

package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.AutoScaleVmProfileDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface AutoScaleVmProfileDetailsDao extends GenericDao<AutoScaleVmProfileDetailVO, Long>, ResourceDetailsDao<AutoScaleVmProfileDetailVO> {

}

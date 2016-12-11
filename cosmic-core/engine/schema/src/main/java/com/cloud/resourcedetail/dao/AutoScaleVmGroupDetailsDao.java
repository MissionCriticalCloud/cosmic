package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.AutoScaleVmGroupDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface AutoScaleVmGroupDetailsDao extends GenericDao<AutoScaleVmGroupDetailVO, Long>, ResourceDetailsDao<AutoScaleVmGroupDetailVO> {

}

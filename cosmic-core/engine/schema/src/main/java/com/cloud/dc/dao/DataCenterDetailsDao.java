package com.cloud.dc.dao;

import com.cloud.dc.DataCenterDetailVO;
import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.utils.db.GenericDao;

public interface DataCenterDetailsDao extends GenericDao<DataCenterDetailVO, Long>, ResourceDetailsDao<DataCenterDetailVO> {
}

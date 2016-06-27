package com.cloud.dc.dao;

import com.cloud.dc.DataCenterDetailVO;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface DataCenterDetailsDao extends GenericDao<DataCenterDetailVO, Long>, ResourceDetailsDao<DataCenterDetailVO> {
}

package com.cloud.service.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.service.ServiceOfferingDetailsVO;
import com.cloud.utils.db.GenericDao;

public interface ServiceOfferingDetailsDao extends GenericDao<ServiceOfferingDetailsVO, Long>, ResourceDetailsDao<ServiceOfferingDetailsVO> {
}

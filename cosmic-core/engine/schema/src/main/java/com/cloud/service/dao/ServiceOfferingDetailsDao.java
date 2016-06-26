package com.cloud.service.dao;

import com.cloud.service.ServiceOfferingDetailsVO;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface ServiceOfferingDetailsDao extends GenericDao<ServiceOfferingDetailsVO, Long>, ResourceDetailsDao<ServiceOfferingDetailsVO> {
}

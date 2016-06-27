package com.cloud.storage.dao;

import com.cloud.storage.VMTemplateDetailVO;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.resourcedetail.ResourceDetailsDao;

public interface VMTemplateDetailsDao extends GenericDao<VMTemplateDetailVO, Long>, ResourceDetailsDao<VMTemplateDetailVO> {

}

package com.cloud.storage.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.storage.VMTemplateDetailVO;
import com.cloud.utils.db.GenericDao;

public interface VMTemplateDetailsDao extends GenericDao<VMTemplateDetailVO, Long>, ResourceDetailsDao<VMTemplateDetailVO> {

}

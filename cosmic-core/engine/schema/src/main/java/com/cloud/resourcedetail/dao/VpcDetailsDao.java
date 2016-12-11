package com.cloud.resourcedetail.dao;

import com.cloud.resourcedetail.ResourceDetailsDao;
import com.cloud.resourcedetail.VpcDetailVO;
import com.cloud.utils.db.GenericDao;

public interface VpcDetailsDao extends GenericDao<VpcDetailVO, Long>, ResourceDetailsDao<VpcDetailVO> {

}
